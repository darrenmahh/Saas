package org.getoffer.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.getoffer.shortlink.admin.dao.entity.UserDO;
import org.getoffer.shortlink.admin.dao.mapper.UserMapper;
import org.getoffer.shortlink.admin.dto.resq.UserRespActualDTO;
import org.getoffer.shortlink.admin.dto.resq.UserRespDTO;
import org.getoffer.shortlink.admin.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/*
* 用户接口实现层
* */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

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
}
