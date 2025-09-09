package org.getoffer.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.getoffer.shortlink.admin.dao.entity.UserDO;
import org.getoffer.shortlink.admin.dto.resq.UserRespDTO;

public interface UserService extends IService<UserDO> {
    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 返回用户信息
     */
    UserRespDTO getUserByUsername(String username);
}
