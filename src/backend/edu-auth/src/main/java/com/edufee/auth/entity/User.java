package com.edufee.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.edufee.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 * 对应数据库 sys_user 表，用于登录认证和权限管理
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户名（登录账号） */
    private String username;

    /** 密码（BCrypt加密存储） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatar;

    /** 性别: 0-未知 1-男 2-女 */
    private Integer gender;

    /** 状态: 0-禁用 1-启用 */
    private Integer status;

    /** 角色编码: ADMIN-管理员 FINANCE-财务 STAFF-普通员工 TEACHER-教师 */
    private String roleCode;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

}
