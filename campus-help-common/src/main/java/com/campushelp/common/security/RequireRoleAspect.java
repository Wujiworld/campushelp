package com.campushelp.common.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class RequireRoleAspect {

    @Around("@annotation(com.campushelp.common.security.RequireRole) || @within(com.campushelp.common.security.RequireRole)")
    public Object enforceRole(ProceedingJoinPoint pjp) throws Throwable {
        RequireRole onMethod = null;
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        onMethod = AnnotationUtils.findAnnotation(method, RequireRole.class);
        RequireRole onClass = AnnotationUtils.findAnnotation(pjp.getTarget().getClass(), RequireRole.class);
        RequireRole ann = onMethod != null ? onMethod : onClass;
        if (ann == null || ann.value().length == 0) {
            return pjp.proceed();
        }
        SecurityContextUtils.requireAnyRole(ann.value());
        return pjp.proceed();
    }
}
