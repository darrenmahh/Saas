package org.getoffer.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.getoffer.shortlink.admin.common.convention.Exception.ClientException;
import org.getoffer.shortlink.admin.dao.entity.UserDO;
import org.getoffer.shortlink.admin.dao.mapper.UserMapper;
import org.getoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import org.getoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.getoffer.shortlink.admin.dto.req.UserUpdateReqDTO;
import org.getoffer.shortlink.admin.dto.resq.UserLoginRespDTO;
import org.getoffer.shortlink.admin.dto.resq.UserRespActualDTO;
import org.getoffer.shortlink.admin.dto.resq.UserRespDTO;
import org.getoffer.shortlink.admin.service.UserService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.getoffer.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static org.getoffer.shortlink.admin.common.enums.UserErrorCodeEnum.*;

/*
* 用户接口实现层
* */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final StringRedisTemplate stringRedisTemplate;
    // 布隆过滤器
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    private final RedissonClient redissonClient;


    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 返回加密的用户信息
     */
    @Override
    public UserRespDTO getUserByUsername(String username) {
        System.out.println("=== 开始查询用户：" + username + " ===");

        try {
            LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                    .eq(UserDO::getUsername, username);
            System.out.println("=== 查询条件构建完成 ===");

            UserDO userDO = baseMapper.selectOne(queryWrapper);
            System.out.println("=== 数据库查询完成，结果：" + userDO + " ===");

            if (userDO == null) {
                System.out.println("=== 警告：未找到用户数据 ===");
                return null;
            }

            UserRespDTO result = new UserRespDTO();
            BeanUtils.copyProperties(userDO, result);
            System.out.println("=== 返回结果：" + result + " ===");

            return result;
        } catch (Exception e) {
            System.out.println("=== 错误发生：" + e.getMessage() + " ===");
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 根据用户名获得用户真实名字
     * @param username 用户名
     * @return 返回用户真实信息返回实体
     */
    @Override
    public UserRespActualDTO getUserActualByUsername(String username) {
        System.out.println("=== 开始查询用户：" + username + " ===");

        try {
            LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getUsername, username);
            System.out.println("=== 查询条件构建完成 ===");

            UserDO userDO = baseMapper.selectOne(queryWrapper);
            System.out.println("=== 数据库查询完成，结果：" + userDO + " ===");

            if (userDO == null) {
                System.out.println("=== 警告：未找到用户数据 ===");
                return null;
            }

            UserRespActualDTO result = new UserRespActualDTO();
            BeanUtils.copyProperties(userDO, result);
            System.out.println("=== 返回结果：" + result + " ===");

            return result;
        } catch (Exception e) {
            System.out.println("=== 错误发生：" + e.getMessage() + " ===");
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 查找数据库是否有当前用户
     * @param username 用户名
     * @return 存在返回true 不存在返回false
     */
    @Override
    public Boolean hasUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    /**
     * 用户注册
     * @param reqDTO 注册请求实体
     */
    @Override
    public void register(UserRegisterReqDTO reqDTO) {
        if(hasUsername(reqDTO.getUsername())){
            throw new ClientException(USER__NAME_EXISTS);
        }
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + reqDTO.getUsername());

        try {
            if (lock.tryLock()) {
                int insert = baseMapper.insert(BeanUtil.toBean(reqDTO, UserDO.class));
                if (insert < 1) {
                    throw new ClientException(USER_SAVE_ERROR);
                }
                userRegisterCachePenetrationBloomFilter.add(reqDTO.getUsername());
                return;
            }
            throw new ClientException(USER__NAME_EXISTS);
        } finally {
            lock.unlock();
        }


    }

    @Override
    public void update(UserUpdateReqDTO reqDTO) {
        // TODO 此处需要判断是否是登录用户修改自己的信息
        if (!hasUsername((reqDTO.getUsername()))) {
            throw new ClientException(USER_NULL);
        }
        LambdaUpdateWrapper<UserDO> eq = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, reqDTO.getUsername());
        int update = baseMapper.update(BeanUtil.toBean(reqDTO, UserDO.class), eq);
    }

    /**
     * 登录service实现
     * @param reqDTO 登录请求实体
     * @return 用户登录返回实体
     */
    @Override
    public UserLoginRespDTO login(UserLoginReqDTO reqDTO) {
        LambdaQueryWrapper<UserDO> eq = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, reqDTO.getUsername())
                .eq(UserDO::getPassword, reqDTO.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(eq);
        if (userDO == null) {
            throw new ClientException(USER_NULL);
        }
        Boolean hasKey = stringRedisTemplate.hasKey("login_" + reqDTO.getUsername());
        if (hasKey != null && hasKey) {
            throw new ClientException("用户已登录，请勿重复登录");
        }
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put("login_" + reqDTO.getUsername(), "token", uuid);
        stringRedisTemplate.expire("login_" + reqDTO.getUsername(), 30L, TimeUnit.MINUTES);
        return new UserLoginRespDTO(uuid);
    }

    /**
     * 检查登录状态
     * @param token 登录 token
     * @return true 已登录 false 未登录
     */
    @Override
    public Boolean checkLogin(String username, String token) {
        Object remoteToken = stringRedisTemplate.opsForHash().get("login_" + username, "token");
        System.out.println("=== 检查登录状态，远程 token：" + remoteToken + "，本地 token：" + token + " ===");
        return remoteToken != null && Objects.equals(remoteToken.toString(), token);
    }

    /**
     * 用户登出
     * @param username 用户名
     */
    @Override
    public void logOut(String username, String token) {
        if (!checkLogin(username, token)) {
            throw new ClientException("用户未登录，无法登出");
        }
        Object hasLogin = stringRedisTemplate.opsForHash().get("login_" + username, "token");
        if (hasLogin != null) {
            stringRedisTemplate.delete("login_" + username);
        }
    }
}
