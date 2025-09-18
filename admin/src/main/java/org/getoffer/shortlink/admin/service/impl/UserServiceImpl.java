package org.getoffer.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.getoffer.shortlink.admin.common.convention.Exception.ClientException;
import org.getoffer.shortlink.admin.dao.entity.UserDO;
import org.getoffer.shortlink.admin.dao.mapper.UserMapper;
import org.getoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.getoffer.shortlink.admin.dto.resq.UserRespActualDTO;
import org.getoffer.shortlink.admin.dto.resq.UserRespDTO;
import org.getoffer.shortlink.admin.service.UserService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.getoffer.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static org.getoffer.shortlink.admin.common.enums.UserErrorCodeEnum.USER_SAVE_ERROR;
import static org.getoffer.shortlink.admin.common.enums.UserErrorCodeEnum.USER__NAME_EXISTS;

/*
* 用户接口实现层
* */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    // 布隆过滤器
    @Autowired
    private RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    @Autowired
    private RedissonClient redissonClient;


    @Override
    public UserRespDTO getUserByUsername(String username) {
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

    @Override
    public Boolean hasUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

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
}
