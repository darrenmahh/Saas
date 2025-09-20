package org.getoffer.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.getoffer.shortlink.admin.common.convention.Exception.ClientException;
import org.getoffer.shortlink.admin.common.convention.result.Result;
import org.getoffer.shortlink.admin.common.convention.result.Results;
import org.getoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import org.getoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.getoffer.shortlink.admin.dto.req.UserUpdateReqDTO;
import org.getoffer.shortlink.admin.dto.resq.UserLoginRespDTO;
import org.getoffer.shortlink.admin.dto.resq.UserRespActualDTO;
import org.getoffer.shortlink.admin.dto.resq.UserRespDTO;
import org.getoffer.shortlink.admin.service.UserService;
import org.springframework.web.bind.annotation.*;

import static org.getoffer.shortlink.admin.common.convention.errorcode.BaseErrorCode.USER_NAME_NULL_ERROR;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名获取用户信息，包含脱敏后的手机号等信息
     * @param username
     * @return
     */
    @GetMapping("/api/short-link/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {
        return Results.success(userService.getUserByUsername(username));
    }

    /**
     * 获取用户的真实信息，包含手机号等敏感信息
     * @param username 用户名
     * @return 返回用户真实信息
     */
    @GetMapping("/api/short-link/v1/actual/user/{username}")
    public Result<UserRespActualDTO> getUserActualByUsername(@PathVariable("username") String username) {
        return Results.success(userService.getUserActualByUsername(username));
    }

    /**
     * 判断用户名是否存在
     * @param username 用户名
     * @return 存在返回true 不存在返回false
     */
    @GetMapping("/api/short-link/v1/user/has-username")
    public Result<Boolean> hasUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ClientException(USER_NAME_NULL_ERROR);
        }
        return  Results.success(userService.hasUsername(username));
    }

    /**
     * 用户注册
     * @param reqDTO 注册请求实体
     * @return 注册结果
     */
    @PostMapping("/api/short-link/v1/user/register")
    public Result<Void> register(@RequestBody UserRegisterReqDTO reqDTO) {
        System.out.println("=== 开始注册用户：" + reqDTO.getUsername() + " ===");
        userService.register(reqDTO);
        return Results.success();
    }

    /**
     *
     * @param reqDTO 修改请求实体
     * @return 修改结果
     */
    @PutMapping("/api/short-link/v1/user/update")
    public Result<Void> update(@RequestBody UserUpdateReqDTO reqDTO) {
        System.out.println("=== 开始修改用户：" + reqDTO.getUsername() + " ===");
        userService.update(reqDTO);
        return Results.success();
    }

    /**
     *  用户登录
     * @param reqDTO 登录请求实体
     * @return 返回登录返回实体
     */
    @PostMapping("/api/short-link/v1/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO reqDTO) {
        System.out.println("=== 开始登录用户：" + reqDTO.getUsername() + " ===");
        UserLoginRespDTO result =  userService.login(reqDTO);
        return Results.success(result);
    }

    /**
     * 检查用户登录状态
     * @param token 登录token
     * @return 登录返回true 未登录返回false
     */
    @GetMapping("/api/short-link/v1/user/check-login")
    public Result<Boolean> checkLogin(@RequestParam("username") String username,@RequestParam("token") String token) {
        return Results.success(userService.checkLogin(username, token));
    }


    @DeleteMapping("/api/short-link/v1/user/logout")
    public Result<Void> logout(@RequestParam("username") String username, @RequestParam("token") String token) {
        // 目前前端不需要登出功能，后续如果需要可以补充
        userService.logOut(username, token);
        return Results.success();
    }
}
