package com.campushelp.common.web;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * 为每个请求生成或透传 X-Request-Id，写入请求属性并回写响应头。
 */
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String ATTR_REQUEST_ID = "X-Request-Id";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String rid = request.getHeader(HEADER_REQUEST_ID);
        if (rid == null || rid.isBlank()) {
            rid = UUID.randomUUID().toString().replace("-", "");
        }
        request.setAttribute(ATTR_REQUEST_ID, rid);
        response.setHeader(HEADER_REQUEST_ID, rid);
        response.setHeader("X-Api-Version", "3");
        filterChain.doFilter(request, response);
    }
}
