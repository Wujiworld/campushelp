package com.campushelp.common.security;

/**
 * 与 {@code ch_role.code} 一致；JWT / Spring Security 权限为 {@code ROLE_} 前缀 + code。
 */
public enum RoleEnum {
    STUDENT("STUDENT"),
    RIDER("RIDER"),
    MERCHANT("MERCHANT"),
    ADMIN("ADMIN");

    private final String code;

    RoleEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String authority() {
        return "ROLE_" + code;
    }
}
