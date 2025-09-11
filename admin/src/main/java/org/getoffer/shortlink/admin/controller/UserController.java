package org.getoffer.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.getoffer.shortlink.admin.common.convention.Exception.ClientException;
import org.getoffer.shortlink.admin.common.convention.result.Result;
import org.getoffer.shortlink.admin.common.convention.result.Results;
import org.getoffer.shortlink.admin.common.enums.UserErrorCodeEnum;
import org.getoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.getoffer.shortlink.admin.dto.resq.UserRespActualDTO;
import org.getoffer.shortlink.admin.dto.resq.UserRespDTO;
import org.getoffer.shortlink.admin.service.UserService;
import org.springframework.web.bind.annotation.*;

import static org.getoffer.shortlink.admin.common.enums.UserErrorCodeEnum.USER__NAME_EXISTS;

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

    /**
     * 判断用户名是否存在
     * @param username 用户名
     * @return 存在返回true 不存在返回false
     */
    @GetMapping("/api/shortlink/v1/user/has-username")
    public Result<Boolean> hasUsername(String username) {
        return  Results.success(userService.hasUsername(username));
    }

    @PostMapping("/api/shortlink/v1/user/register")
    public Result<Void> register(@RequestBody UserRegisterReqDTO reqDTO) {
        if (userService.hasUsername(reqDTO.getUsername())) {
            throw new ClientException(USER__NAME_EXISTS);
        }
        System.out.println("=== 开始注册用户：" + reqDTO.getUsername() + " ===");
        userService.register(reqDTO);
        return Results.success();
    }
}
