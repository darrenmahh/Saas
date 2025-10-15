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
import org.getoffer.shortlink.project.toolkit.LinkUtil;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.getoffer.shortlink.project.common.constant.RedisKeyConstant.*;


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

        stringRedisTemplate.opsForValue()
                .set(fullShortUrl,reqDTO.getOriginUrl(),
                        LinkUtil.getLinkCacheValidTime(reqDTO.getValidDate()),TimeUnit.MILLISECONDS);

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
     * 短链接跳转到原始链接
     * 
     * 核心功能：根据短链接码查询原始URL并执行302重定向
     * 性能优化：三级缓存 + 布隆过滤器 + 分布式锁机制
     * 
     * 解决的问题：
     * 1. 缓存穿透：使用布隆过滤器快速过滤不存在的短链接
     * 2. 缓存击穿：使用分布式锁 + Double-Check 防止热点数据失效时的并发查库
     * 3. 性能优化：95%+ 的请求直接从缓存返回，不查询数据库
     * 
     * @param shortUri 短链接标识符（如：abc123）
     * @param request  Servlet请求对象，用于获取域名和协议信息
     * @param response Servlet响应对象，用于执行重定向
     */
    @SneakyThrows  // Lombok注解：自动处理 sendRedirect 抛出的 IOException，简化异常处理
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        // ==================== 第一步：构建完整短链接URL ====================
        // 从请求中提取域名和协议信息，拼接成完整的短链接URL
        String serverName = request.getServerName();      // 获取请求的服务器域名，如：nurl.ink
        String scheme = request.getScheme();              // 获取请求协议，如：http 或 https
        String fullShortUrl = scheme + "://" + serverName + "/" + shortUri;  // 拼接完整URL，如：http://nurl.ink/abc123

        // ==================== 第二步：第一次Redis缓存查询（未加锁，快速路径） ====================
        // 尝试从 Redis 缓存中直接获取原始链接（热点数据大概率命中）
        // Key格式：short-link_goto_http://nurl.ink/abc123 -> Value: https://www.baidu.com
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        
        // 缓存命中：如果 Redis 中存在该短链接的缓存数据
        if (StrUtil.isNotBlank(originalLink)) {
            // 直接执行 HTTP 302 重定向到原始链接，性能最优（约95%的请求在此返回）
            ((HttpServletResponse) response).sendRedirect(originalLink);
            return;  // 快速返回，无需后续的锁、数据库查询等操作
        }

        // ==================== 第三步：布隆过滤器防止缓存穿透 ====================
        // 缓存未命中后，使用布隆过滤器判断短链接是否可能存在于系统中
        // 布隆过滤器特性：
        //   - 如果返回 false，则短链接 100% 不存在（可以直接拦截）
        //   - 如果返回 true，则短链接可能存在（存在一定误判率，需要继续查询）
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        
        // 布隆过滤器判定为不存在：说明该短链接从未被创建过
        if (!contains) {
            // 直接返回，不查询数据库，有效防止恶意请求穿透缓存（缓存穿透攻击）
            // 例如：攻击者批量请求 /xxxxxx、/yyyyyy 等不存在的短链接
            return;
        }

        // ==================== 第四步：缓存空值检查（防止缓存穿透的二次防御） ====================
        // 再次检查缓存，判断该短链接是否被标记为"不存在"（缓存空值）
        // 如果之前已经查询过并确认不存在，会在缓存中存储一个空值标记
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        
        // 如果缓存中有空值标记，说明这是一个已确认不存在的短链接
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            // 直接返回，避免重复查询数据库（防止布隆过滤器误判导致的缓存穿透）
            return;
        }
        // ==================== 第五步：获取分布式锁（防止缓存击穿） ====================
        // 缓存未命中且布隆过滤器判定可能存在时，需要查询数据库
        // 为避免高并发场景下大量请求同时查询数据库（缓存击穿），使用分布式锁控制并发
        // 创建基于 fullShortUrl 的分布式锁，每个短链接对应一把独立的锁（锁粒度细，性能好）
        // Lock Key格式：short-link_lock_goto_http://nurl.ink/abc123
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        
        // 阻塞式加锁：同一时刻只允许一个线程获取该锁，其他线程在此等待
        // 场景：热点短链接缓存失效时，1000个并发请求中只有1个去查数据库，其余999个等待
        lock.lock();

        try {
            // ==================== 第六步：Double-Check 双重检查缓存（重要！） ====================
            // 为什么要再次检查缓存？
            // 假设有100个线程同时到达第五步等待锁：
            //   - 第1个线程：获取锁 -> 查询数据库 -> 写入缓存 -> 释放锁
            //   - 第2-100个线程：等锁期间，第1个线程已经写入了缓存
            // 所以获取锁后，先检查缓存是否已存在，避免重复查询数据库（Double-Check 设计模式）
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            
            // 如果缓存已存在（说明在等锁期间，其他线程已完成数据库查询并写入缓存）
            if (StrUtil.isNotBlank(originalLink)) {
                // 直接重定向，无需再查数据库（大部分等待锁的线程都会在此返回）
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;  // 快速返回，节省数据库资源
            }

            // ==================== 第七步：查询路由表（Goto表）获取分组标识 ====================
            // 到这里说明缓存确实不存在，需要查询数据库
            // 第一步：查询 t_link_goto 路由表，获取短链接对应的 GID（分组标识）
            // Goto表作用：存储 完整短链接 -> GID 的映射关系，类似于路由表
            LambdaQueryWrapper<ShortLinkGotoDO> gotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);  // 查询条件：完整短链接
            
            // 执行查询，获取路由记录
            ShortLinkGotoDO linkGotoDO = shortLinkGotoMapper.selectOne(gotoQueryWrapper);
            
            // 如果路由表中不存在该记录，说明短链接不存在或已被删除
            if (linkGotoDO == null) {
                // 防止空值
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                return;  // 直接返回，不进行重定向
            }

            // ==================== 第八步：根据GID查询主表获取完整信息 ====================
            // 第二步：根据 GID 查询 t_link 主表，获取短链接的完整信息（包括原始URL）
            // 为什么要分两步查询？
            //   - 主表 t_link 按 GID 进行分库分表，GID 是分片键
            //   - 只有知道 GID，才能精确定位到具体的分表，提升查询性能
            //   - 如果直接用 fullShortUrl 查主表，会导致全表扫描（性能极差）
            LambdaQueryWrapper<ShortLinkDO> linkQueryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, linkGotoDO.getGid())           // 分片键：定位分表
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)         // 完整短链接
                    .eq(ShortLinkDO::getDelFlag, 0)                         // 未删除（逻辑删除标记）
                    .eq(ShortLinkDO::getEnableStatus, 0);                   // 启用状态（0=启用，1=禁用）
            
            // 执行查询，获取短链接完整数据对象
            ShortLinkDO shortLinkDO = baseMapper.selectOne(linkQueryWrapper);

            // ==================== 第九步：写入缓存并执行重定向 ====================
            // 如果查询到短链接记录（正常情况）
            if (shortLinkDO != null) {
                // 将原始链接写入 Redis 缓存，供后续请求快速访问
                // Key: short-link_goto_http://nurl.ink/abc123
                // Value: https://www.baidu.com
                // TODO: 生产环境建议设置过期时间，例如：7天或30天
                // stringRedisTemplate.opsForValue().set(key, value, 7, TimeUnit.DAYS);
                stringRedisTemplate.opsForValue().set(
                        String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                        shortLinkDO.getOriginUrl()
                );
                
                // 执行 HTTP 302 临时重定向到原始链接
                // 浏览器收到302响应后，会自动跳转到 Location 头指定的 URL
                ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
            }
            // 如果 shortLinkDO 为 null（数据不一致，Goto表有记录但主表没有）
            // TODO: 建议记录异常日志，方便排查数据一致性问题
            
        } finally {
            // ==================== 第十步：释放分布式锁（必须执行） ====================
            // finally 块保证无论是否发生异常，都会释放锁
            // 如果不释放锁，其他线程会一直阻塞等待，造成死锁
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
