package com.campushelp.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 登录主体：用 userId 作为业务主键（手机号登录在 {@link com.campushelp.user} 服务中解析为 userId）。
 * <p>
 * 说明：Spring Security 的 {@link UserDetails#getUsername()} 约定返回字符串，
 * 这里返回 userId 的字符串形式，便于日志与审计；业务代码请优先使用 {@link #getUserId()}。
 */
public class CampusUserPrincipal implements UserDetails {

    private final Long userId;
    private final String passwordPlaceholder;
    private final Collection<? extends GrantedAuthority> authorities;

    public CampusUserPrincipal(Long userId,
                               Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.authorities = authorities;
        this.passwordPlaceholder = "N/A";
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordPlaceholder;
    }

    @Override
    public String getUsername() {
        return String.valueOf(userId);
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
        return true;
    }
}
