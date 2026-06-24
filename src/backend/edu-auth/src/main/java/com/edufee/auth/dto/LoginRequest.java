package com.edufee.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 * 前端提交的登录表单数据
 */
@Data
public class LoginRequest {

    /** 用户名，不能为空 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码，不能为空 */
    @NotBlank(message = "密码不能为空")
    private String password;

}
