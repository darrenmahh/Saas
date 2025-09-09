package org.getoffer.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.getoffer.shortlink.admin.common.convention.result.Result;
import org.getoffer.shortlink.admin.common.convention.result.Results;
import org.getoffer.shortlink.admin.common.enums.UserErrorCodeEnum;
import org.getoffer.shortlink.admin.dto.resq.UserRespActualDTO;
import org.getoffer.shortlink.admin.dto.resq.UserRespDTO;
import org.getoffer.shortlink.admin.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名获取用户信息，包含脱敏后的手机号等信息
     * @param username
     * @return
     */
    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {
        return Results.success(userService.getUserByUsername(username));
    }

    /**
     * 获取用户的真实信息，包含手机号等敏感信息
     * @param username
     * @return
     */
    @GetMapping("/api/shortlink/v1/actual/user/{username}")
    public Result<UserRespActualDTO> getUserActualByUsername(@PathVariable("username") String username) {
        return Results.success(userService.getUserActualByUsername(username));
    }
}
