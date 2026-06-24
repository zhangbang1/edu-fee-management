package com.edufee.auth.service;

import com.edufee.auth.dto.LoginRequest;
import com.edufee.auth.dto.LoginResponse;
import com.edufee.auth.entity.User;

/**
 * 认证授权服务接口
 * 定义用户登录、登出、获取当前用户等核心认证方法
 */
public interface AuthService {

    /**
     * 用户登录
     * 验证用户名密码，生成JWT令牌返回
     *
     * @param request 登录请求（用户名、密码）
     * @return 登录响应（JWT令牌、用户信息）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户登出
     * 清除当前登录状态（JWT无状态，前端删除token即可，后端记录登出日志）
     */
    void logout();

    /**
     * 获取当前登录用户信息
     *
     * @return 当前用户实体
     */
    User getCurrentUser();

}
