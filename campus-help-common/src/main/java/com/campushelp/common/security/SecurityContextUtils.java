package com.campushelp.common.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 从 Spring Security 上下文读取当前登录用户 ID、校验角色。
 */
public final class SecurityContextUtils {

    private SecurityContextUtils() {
    }

    public static Long requireUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new IllegalStateException("未登录或登录已失效");
        }
        Object p = auth.getPrincipal();
        if (p instanceof CampusUserPrincipal) {
            return ((CampusUserPrincipal) p).getUserId();
        }
        throw new IllegalStateException("无法解析当前用户");
    }

    /** 未登录或匿名时返回 0 */
    public static long optionalUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return 0L;
        }
        Object p = auth.getPrincipal();
        if (p instanceof CampusUserPrincipal) {
            return ((CampusUserPrincipal) p).getUserId();
        }
        return 0L;
    }

    /**
     * @param roleCode 角色编码，与库表 ch_role.code 一致（不含 {@code ROLE_} 前缀）
     */
    public static void requireRole(String roleCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("需要登录");
        }
        String need = "ROLE_" + roleCode;
        boolean ok = false;
        for (GrantedAuthority a : auth.getAuthorities()) {
            if (need.equalsIgnoreCase(a.getAuthority())) {
                ok = true;
                break;
            }
        }
        if (!ok) {
            throw new AccessDeniedException("需要角色: " + roleCode);
        }
    }

    public static boolean hasRole(String roleCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        String need = "ROLE_" + roleCode;
        for (GrantedAuthority a : auth.getAuthorities()) {
            if (need.equalsIgnoreCase(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 具备任一角色即可；否则 {@link AccessDeniedException}。
     */
    public static void requireAnyRole(RoleEnum... roles) {
        if (roles == null || roles.length == 0) {
            return;
        }
        for (RoleEnum r : roles) {
            if (hasRole(r.getCode())) {
                return;
            }
        }
        StringBuilder sb = new StringBuilder("需要角色之一: ");
        for (int i = 0; i < roles.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(roles[i].getCode());
        }
        throw new AccessDeniedException(sb.toString());
    }
}
