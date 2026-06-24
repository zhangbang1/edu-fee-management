package com.edufee.auth.service.impl;

import com.edufee.auth.dto.LoginRequest;
import com.edufee.auth.dto.LoginResponse;
import com.edufee.auth.entity.User;
import com.edufee.auth.mapper.UserMapper;
import com.edufee.auth.security.JwtUserDetails;
import com.edufee.auth.security.JwtUtil;
import com.edufee.auth.service.AuthService;
import com.edufee.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 认证授权服务实现类
 * 实现用户登录验证、JWT生成、登出、获取当前用户等核心逻辑
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private AuthenticationManager authenticationManager;

    /**
     * 用户登录
     * 实现流程:
     * 1. 查询用户是否存在
     * 2. 检查账户状态是否启用
     * 3. 使用Spring Security的AuthenticationManager验证密码
     * 4. 生成JWT令牌
     * 5. 更新最近登录时间
     * 6. 返回登录响应
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        // 1. 查询用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            log.warn("登录失败: 用户 {} 不存在", username);
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 2. 检查账户状态
        if (user.getStatus() == 0) {
            log.warn("登录失败: 用户 {} 已被禁用", username);
            throw new BusinessException(401, "账户已被禁用，请联系管理员");
        }

        // 3. 使用AuthenticationManager验证密码
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            // 将认证结果设置到SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadCredentialsException e) {
            log.warn("登录失败: 用户 {} 密码错误", username);
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 4. 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoleCode());

        // 5. 更新最近登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户 {} ({}) 登录成功, 角色: {}", user.getUsername(), user.getRealName(), user.getRoleCode());

        // 6. 构建登录响应
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roleCode(user.getRoleCode())
                .build();
    }

    /**
     * 用户登出
     * JWT无状态模式，登出只需清除Spring Security上下文
     * 前端负责删除本地存储的token
     */
    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
        log.info("用户已登出");
    }

    /**
     * 获取当前登录用户信息
     * 从Spring Security上下文中提取用户详情
     */
    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "用户未登录");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserDetails) {
            JwtUserDetails userDetails = (JwtUserDetails) principal;
            User user = userMapper.selectByUsername(userDetails.getUsername());
            if (user != null) {
                // 清除密码后返回
                user.setPassword(null);
            }
            return user;
        }

        throw new BusinessException(401, "无法获取当前用户信息");
    }

}
