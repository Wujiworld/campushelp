package com.campushelp.common.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法或类：当前用户需具备<strong>任一</strong>列出角色（与 {@link SecurityContextUtils#requireAnyRole} 一致）。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequireRole {
    RoleEnum[] value();
}
