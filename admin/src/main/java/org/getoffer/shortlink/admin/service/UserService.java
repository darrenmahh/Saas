package org.getoffer.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.getoffer.shortlink.admin.dao.entity.UserDO;
import org.getoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import org.getoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.getoffer.shortlink.admin.dto.req.UserUpdateReqDTO;
import org.getoffer.shortlink.admin.dto.resq.UserLoginRespDTO;
import org.getoffer.shortlink.admin.dto.resq.UserRespActualDTO;
import org.getoffer.shortlink.admin.dto.resq.UserRespDTO;

public interface UserService extends IService<UserDO> {
    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 返回用户信息
     */
    UserRespDTO getUserByUsername(String username);

    UserRespActualDTO getUserActualByUsername(String username);

    /**
     * 判断用户名是否存在
     * @param username 用户名
     * @return 存在返回true 不存在返回false
     */
    Boolean hasUsername(String username);

    void register(UserRegisterReqDTO reqDTO);

    /**
     *  根据用户名修改用户
     * @param reqDTO 修改用户请求实体
     */
    void update(UserUpdateReqDTO reqDTO);

    UserLoginRespDTO login(UserLoginReqDTO reqDTO);

    Boolean checkLogin(String username, String token);
}
