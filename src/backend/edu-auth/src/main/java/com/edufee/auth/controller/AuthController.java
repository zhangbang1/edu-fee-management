package com.edufee.auth.controller;

import com.edufee.auth.dto.LoginRequest;
import com.edufee.auth.dto.LoginResponse;
import com.edufee.auth.entity.User;
import com.edufee.auth.service.AuthService;
import com.edufee.common.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 认证授权控制器
 * 提供登录、登出、获取当前用户信息等接口
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证授权", description = "用户登录、登出、获取当前用户信息")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * 用户登录接口
     * 验证用户名密码，返回JWT令牌
     * 无需认证即可访问
     *
     * @param request 登录请求（用户名、密码）
     * @return JWT令牌及用户信息
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名密码登录，返回JWT访问令牌")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return R.ok("登录成功", response);
    }

    /**
     * 用户登出接口
     * 清除服务端Security上下文
     *
     * @return 操作结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出当前登录，清除认证状态")
    @PreAuthorize("isAuthenticated()")
    public R<Void> logout() {
        authService.logout();
        return R.ok("已成功退出登录");
    }

    /**
     * 获取当前登录用户信息
     * 从JWT令牌中解析用户信息并返回
     *
     * @return 当前用户信息（不含密码）
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "返回当前登录用户的详细信息")
    @PreAuthorize("isAuthenticated()")
    public R<User> me() {
        User user = authService.getCurrentUser();
        return R.ok(user);
    }

}
