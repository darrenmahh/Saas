package org.getoffer.shortlink.admin.dto.resq;

import lombok.Data;

/**
* 用户返回实体
*/
@Data
public class UserRespDTO {
    private Long id;

    private String username;

    private String realName;

    private String phone;

    private String mail;

}
