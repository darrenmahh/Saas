package org.getoffer.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user")
public class UserDO {
    private Long id;

    private String username;

    private String password;

    private String realName;

    private String phone;

    private String mail;

    private Long deletionTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer delFlag;
}
