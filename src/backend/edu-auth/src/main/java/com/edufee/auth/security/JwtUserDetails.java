package com.edufee.auth.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * JWT用户详情
 * 实现Spring Security的UserDetails接口，封装当前登录用户信息
 * 用于Spring Security的认证和授权流程
 */
@Getter
public class JwtUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private final Long userId;

    /** 用户名 */
    private final String username;

    /** 密码 */
    private final String password;

    /** 真实姓名 */
    private final String realName;

    /** 角色编码 */
    private final String roleCode;

    /** 账户是否启用 */
    private final boolean enabled;

    /** 权限集合 */
    private final Collection<? extends GrantedAuthority> authorities;

    public JwtUserDetails(Long userId, String username, String password,
                          String realName, String roleCode, boolean enabled) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.roleCode = roleCode;
        this.enabled = enabled;
        // 将角色编码转换为Spring Security权限（格式: ROLE_XXX）
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + roleCode)
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
