package org.getoffer.shortlink.admin.common.biz.user;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;

/**
 * 用户信息传输过滤器
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        String uri = req.getRequestURI();

        // 临时调试
        System.out.println("请求URI: " + uri);
        String username = req.getHeader("username");
        String token = req.getHeader("token");
        System.out.println("请求头 username: [" + username + "]");
        System.out.println("请求头 token: [" + token + "]");

        // 登录/注册放行
        if (uri.startsWith("/api/short-link/v1/user/login")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (org.springframework.util.StringUtils.hasText(username) && org.springframework.util.StringUtils.hasText(token)) {
            // 检查 Redis 中存储的 token 是否匹配
            Object storedToken = stringRedisTemplate.opsForHash().get("login_" + username, "token");

            if (token.equals(storedToken)) {
                // token 验证通过，直接设置用户信息
                UserInfoDTO userInfoDTO = UserInfoDTO.builder()
                        .username(username)
                        .build();
                UserContext.setUser(userInfoDTO);
                System.out.println("用户验证成功，用户名: " + username);
            } else {
                System.out.println("Token 验证失败");
            }
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }
}