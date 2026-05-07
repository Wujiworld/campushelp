package com.campushelp.common.security;

import com.campushelp.common.api.ApiResult;
import com.campushelp.common.api.ResultCode;
import com.campushelp.common.web.RequestIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 未认证访问受保护资源时返回与业务一致的 {@link ApiResult} JSON（HTTP 401）。
 */
@Component
public class ApiJsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public ApiJsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        Object rid = request.getAttribute(RequestIdFilter.ATTR_REQUEST_ID);
        ApiResult<Void> body = ApiResult.fail(ResultCode.UNAUTHORIZED,
                ResultCode.UNAUTHORIZED.getDefaultMessage(),
                rid != null ? rid.toString() : null);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
