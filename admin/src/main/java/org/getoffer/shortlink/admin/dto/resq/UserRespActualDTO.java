package org.getoffer.shortlink.admin.dto.resq;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.getoffer.shortlink.admin.common.serilize.PhoneDesensitizationSerializer;
@Data
public class UserRespActualDTO {

    /**
     * 返回用户ID
     */
    private Long id;

    /**
     * 返回用户名
     */
    private String username;

    /**
     * 返回用户真实姓名
     */
    private String realName;

    /**
     * 返回手机号
     */
    private String phone;

    /**
     * 返回邮箱
     */
    private String mail;

}
