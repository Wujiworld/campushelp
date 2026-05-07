package com.campushelp.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 对 {@link RequireRoleAspect} 做最小可用验证，确保角色门禁生效。
 */
class RequireRoleAspectTest {

    private final RequireRoleAspect aspect = new RequireRoleAspect();

    @Test
    void allowsWhenRoleMatches() {
        SecureDemoService proxy = proxy(new SecureDemoService());
        setAuth(RoleEnum.ADMIN);

        String result = proxy.adminOnly();

        assertEquals("ok", result);
    }

    @Test
    void blocksWhenRoleMissing() {
        SecureDemoService proxy = proxy(new SecureDemoService());
        setAuth(RoleEnum.STUDENT);

        assertThrows(AccessDeniedException.class, proxy::adminOnly);
    }

    private SecureDemoService proxy(SecureDemoService target) {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        return factory.getProxy();
    }

    private void setAuth(RoleEnum role) {
        CampusUserPrincipal principal = new CampusUserPrincipal(1L,
                List.of(new SimpleGrantedAuthority(role.authority())));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @RequireRole(RoleEnum.ADMIN)
    static class SecureDemoService {
        String adminOnly() {
            return "ok";
        }
    }

    @org.junit.jupiter.api.AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }
}
