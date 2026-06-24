package com.edufee.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 登录响应DTO
 * 登录成功后返回的令牌和用户信息
 */
@Data
@Builder
public class LoginResponse {

    /** JWT访问令牌 */
    private String accessToken;

    /** 令牌类型，固定为 Bearer */
    private String tokenType;

    /** 令牌过期时间（秒） */
    private long expiresIn;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 角色编码 */
    private String roleCode;

    /** 用户权限列表 */
    private List<String> permissions;

}
