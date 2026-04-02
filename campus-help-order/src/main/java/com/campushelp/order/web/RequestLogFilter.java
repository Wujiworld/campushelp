package com.campushelp.order.web;

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
 * 目标：让你在 IDEA 控制台一眼看到“请求进来-状态码-耗时”。
 * 替代方案：Spring Boot 的 access log（更工程化，但不一定默认配置）。
 */
@Component
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

