package com.edufee.auth.security;

import com.alibaba.fastjson2.JSON;
import com.edufee.auth.entity.User;
import com.edufee.auth.mapper.UserMapper;
import com.edufee.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * JWT认证过滤器
 * 拦截所有请求，从请求头中提取JWT令牌并验证
 * 验证通过后将用户信息设置到Spring Security上下文中
 * 继承OncePerRequestFilter确保每个请求只执行一次过滤
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private UserMapper userMapper;

    /** 白名单路径，不需要JWT认证 */
    private static final String[] WHITE_LIST = {
            "/api/auth/login",
            "/api/auth/register",
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/favicon.ico"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        // 检查是否为白名单路径
        if (isWhiteListed(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从请求头中获取JWT令牌
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 验证令牌
        if (!jwtUtil.validateToken(token)) {
            writeUnauthorizedResponse(response, "令牌无效或已过期，请重新登录");
            return;
        }

        try {
            // 从令牌中解析用户名
            String username = jwtUtil.getUsernameFromToken(token);

            // 如果SecurityContext中没有认证信息，则设置
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userMapper.selectByUsername(username);

                if (user == null || user.getStatus() == 0) {
                    writeUnauthorizedResponse(response, "用户不存在或已被禁用");
                    return;
                }

                // 构建JwtUserDetails
                JwtUserDetails userDetails = new JwtUserDetails(
                        user.getId(), user.getUsername(), user.getPassword(),
                        user.getRealName(), user.getRoleCode(), user.getStatus() == 1
                );

                // 创建认证令牌并设置到SecurityContext
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("JWT认证失败", e);
            writeUnauthorizedResponse(response, "认证失败：" + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头 Authorization 中提取JWT令牌
     * 支持 Bearer token 格式
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // 也支持直接传token参数
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }
        return null;
    }

    /**
     * 检查请求路径是否在白名单中
     */
    private boolean isWhiteListed(String requestUri) {
        for (String pattern : WHITE_LIST) {
            if (pattern.endsWith("/**")) {
                String prefix = pattern.substring(0, pattern.length() - 3);
                if (requestUri.startsWith(prefix)) {
                    return true;
                }
            } else if (requestUri.equals(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 写入401未授权响应
     */
    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        R<Void> result = R.unauthorized(message);
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSON.toJSONString(result));
            writer.flush();
        }
    }

}
