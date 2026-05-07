package com.campushelp.common.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 请求耗时与状态码日志（单体部署时仅注册一个 Bean，避免与业务模块重复）。
 */
@Component("requestLogFilter")
public class RequestLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long cost = System.currentTimeMillis() - start;
            String query = request.getQueryString();
            String uri = request.getRequestURI();
            String fullPath = (query == null || query.isEmpty()) ? uri : (uri + "?" + query);
            log.info("HTTP {} {} => {} ({} ms)", request.getMethod(), fullPath, response.getStatus(), cost);
        }
    }
}
