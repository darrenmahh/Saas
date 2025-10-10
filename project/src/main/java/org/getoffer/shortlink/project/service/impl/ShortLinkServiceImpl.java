package org.getoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.getoffer.shortlink.admin.common.convention.Exception.ClientException;
import org.getoffer.shortlink.project.common.convention.exception.ServiceException;
import org.getoffer.shortlink.project.common.enums.ValiDateTypeEnum;
import org.getoffer.shortlink.project.dao.entity.ShortLinkDO;
import org.getoffer.shortlink.project.dao.entity.ShortLinkGotoDO;
import org.getoffer.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import org.getoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import org.getoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.getoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.getoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import org.getoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.getoffer.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import org.getoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import org.getoffer.shortlink.project.service.ShortLinkService;
import org.getoffer.shortlink.project.toolkit.HashUtil;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.getoffer.shortlink.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;
import static org.getoffer.shortlink.project.common.constant.RedisKeyConstant.LOCK_GOTO_SHORT_LINK_KEY;


@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {


    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO reqDTO) {

        String shortLinkSuffix = generateSuffix(reqDTO);
        String fullShortUrl = StrBuilder
                .create(reqDTO.getDomain())
                .append("/")
                .append(shortLinkSuffix)
                .toString();

        ShortLinkDO shortLinkDO =  ShortLinkDO.builder()
                .domain(reqDTO.getDomain())
                .originUrl(reqDTO.getOriginUrl())
                .gid(reqDTO.getGid())
                .createdType(reqDTO.getCreatedType())
                .validDateType(reqDTO.getValidDateType())
                .validDate(reqDTO.getValidDate())
                .describe(reqDTO.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .fullShortUrl(fullShortUrl)
                .build();
        ShortLinkGotoDO linkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(reqDTO.getGid())
                .build();
        try {
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(linkGotoDO);
        } catch (Exception e) {
            LambdaQueryWrapper<ShortLinkDO> eq = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, shortLinkDO.getFullShortUrl());
            ShortLinkDO hasShortLink = baseMapper.selectOne(eq);
            if (hasShortLink != null) {
                log.warn("短链接 {} 重复创建",fullShortUrl);
                throw new ServiceException("短链接重复创建");
            }
        }
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(reqDTO.getDomainProtocol() + shortLinkDO.getFullShortUrl())
                .originUrl(reqDTO.getOriginUrl())
                .gid(reqDTO.getGid())
                .build();
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO reqDTO) {
        LambdaQueryWrapper<ShortLinkDO> shortLinkDOLambdaQueryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, reqDTO.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(reqDTO , shortLinkDOLambdaQueryWrapper);
        return resultPage.convert(each -> BeanUtil.toBean(each, ShortLinkPageRespDTO.class));
    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .eq("del_flag", 0)  // 添加删除标记判断
                .groupBy("gid");
        List<Map<String, Object>> shortLinkDOList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOList, ShortLinkGroupCountQueryRespDTO.class);
    }

    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO reqDTO) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, reqDTO.getGid())
                .eq(ShortLinkDO::getFullShortUrl, reqDTO.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDO == null) {
            throw new ClientException("短链接记录不存在");
        }
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(hasShortLinkDO.getDomain())
                .shortUri(hasShortLinkDO.getShortUri())
                .clickNum(hasShortLinkDO.getClickNum())
                .favicon(hasShortLinkDO.getFavicon())
                .createdType(hasShortLinkDO.getCreatedType())
                .gid(reqDTO.getGid())
                .originUrl(reqDTO.getOriginUrl())
                .describe(reqDTO.getDescribe())
                .validDateType(reqDTO.getValidDateType())
                .validDate(reqDTO.getValidDate())
                .build();
        if (Objects.equals(hasShortLinkDO.getGid(), reqDTO.getGid())) {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, reqDTO.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, reqDTO.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(reqDTO.getValidDateType(), ValiDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            baseMapper.update(shortLinkDO, updateWrapper);
        } else {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, reqDTO.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, hasShortLinkDO.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            baseMapper.delete(updateWrapper);
            baseMapper.insert(shortLinkDO);
        }


    }

    /**
     * 短链接跳转原始链接
     * 使用分布式锁 + Double-Check 机制解决缓存击穿问题
     */
    @SneakyThrows  // Lombok注解，自动处理受检异常
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        // ==================== 第一步：构建完整短链接 ====================
        // 获取请求的服务器域名，例如：www.example.com
        String serverName = request.getServerName();
        // 获取请求协议，例如：http 或 https
        String scheme = request.getScheme();
        // 拼接完整短链接 URL，例如：http://www.example.com/abc123
        String fullShortUrl = scheme + "://" + serverName + "/" + shortUri;

        // ==================== 第二步：第一次缓存查询（未加锁） ====================
        // 从 Redis 缓存中尝试获取原始链接（大部分请求在此直接返回，性能最优）
        // GOTO_SHORT_LINK_KEY 格式示例：goto:http://www.example.com/abc123
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        // 如果缓存中存在原始链接（缓存命中）
        if (StrUtil.isNotBlank(originalLink)) {
            // 直接进行 302 重定向到原始链接，无需查询数据库
            ((HttpServletResponse) response).sendRedirect(originalLink);
            return;  // 结束方法，约 95% 的请求在此就结束了
        }

        // ==================== 第三步：缓存未命中，获取分布式锁 ====================
        // 创建基于 fullShortUrl 的分布式锁（每个短链接一把锁，粒度细，并发性能好）
        // LOCK_GOTO_SHORT_LINK_KEY 格式示例：lock:goto:http://www.example.com/abc123
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        // 加锁：同一时刻只允许一个线程通过，其他线程在此阻塞等待
        // 作用：防止缓存击穿，避免大量请求同时查询数据库
        lock.lock();

        try {
            // ==================== 第四步：Double-Check 再次检查缓存 ====================
            // 再次从 Redis 查询缓存（关键！可能在等待锁的过程中，其他线程已经查询并写入缓存）
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            // 如果此时缓存已存在（说明前一个获取锁的线程已经完成了数据库查询和缓存写入）
            if (StrUtil.isNotBlank(originalLink)) {
                // 直接重定向，无需再次查询数据库（避免重复查询，提升性能）
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;  // 结束方法，其他等待锁的线程大部分在此返回
            }

            // ==================== 第五步：查询 Goto 表获取 GID ====================
            // 构建查询条件：根据完整短链接查询 t_link_goto 表
            // Goto 表的作用：存储 短链接 -> GID 的映射关系（路由表）
            LambdaQueryWrapper<ShortLinkGotoDO> eq = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            // 执行查询，获取 Goto 记录
            ShortLinkGotoDO linkGotoDO = shortLinkGotoMapper.selectOne(eq);
            // 如果 Goto 表中不存在该短链接记录（说明短链接不存在或已被删除）
            if (linkGotoDO == null) {
                // TODO: 严格来说此处应该进行风控处理（如记录日志、返回 404 页面等）
                return;  // 直接返回，不进行重定向
            }

            // ==================== 第六步：根据 GID 查询主表获取原始链接 ====================
            // 构建查询条件：根据 GID 和完整短链接查询 t_link 主表
            // 为什么需要 GID？因为主表按 GID 分表，GID 是分片键，可以精确定位到具体分表
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, linkGotoDO.getGid())  // 分片键，定位分表
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)  // 完整短链接
                    .eq(ShortLinkDO::getDelFlag, 0)  // 未删除
                    .eq(ShortLinkDO::getEnableStatus, 0);  // 启用状态
            // 执行查询，获取短链接完整信息
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);

            // ==================== 第七步：写入缓存并重定向 ====================
            // 如果查询到短链接记录
            if (shortLinkDO != null) {
                // 将原始链接写入 Redis 缓存（供后续请求直接使用，无需查库）
                // TODO: 注意：这里没有设置过期时间，实际项目中应该设置合理的过期时间
                stringRedisTemplate.opsForValue().set(
                        String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                        shortLinkDO.getOriginUrl()
                );
                // 执行 302 重定向到原始链接
                ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
            }
            // 如果 shortLinkDO 为 null，则不进行任何操作（可能是数据不一致导致）
        } finally {
            // ==================== 第八步：释放分布式锁 ====================
            // 无论是否发生异常，都必须释放锁（finally 保证一定执行）
            // 释放锁后，其他等待的线程可以继续获取锁执行
            lock.unlock();
        }
    }

    private String generateSuffix(ShortLinkCreateReqDTO reqDTO) {
        int customGenerateCount = 0;
        String shortUri;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接频繁生成，请稍后再试。");
            }
            // 从传入的实体中查找原始uri
            String originUri = reqDTO.getOriginUrl();
            System.out.println("原始完整链接" + originUri);
            // 为原始链接创建唯一的短链接
            originUri += System.currentTimeMillis();
            System.out.println("加上完整链接之后的" + originUri);
            shortUri = HashUtil.hashToBase62(originUri);

            if (!shortUriCreateCachePenetrationBloomFilter.contains(reqDTO.getDomain() + "/" + shortUri)) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }
}
