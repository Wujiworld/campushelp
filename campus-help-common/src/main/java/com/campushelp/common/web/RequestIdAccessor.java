package com.campushelp.common.web;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 从当前请求线程读取 Request-Id（供异常处理等使用）。
 */
public final class RequestIdAccessor {

    private RequestIdAccessor() {
    }

    public static String currentRequestId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        Object v = request.getAttribute(RequestIdFilter.ATTR_REQUEST_ID);
        return v != null ? v.toString() : null;
    }
}
