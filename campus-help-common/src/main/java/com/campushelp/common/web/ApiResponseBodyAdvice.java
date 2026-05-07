package com.campushelp.common.web;

import com.campushelp.common.api.ApiResult;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 将 Controller 返回值统一包装为 {@link ApiResult}（成功场景）。
 * <p>
 * 已返回 {@link ApiResult}、{@link org.springframework.http.ResponseEntity}、{@code String} 的不做包装。
 */
    @ControllerAdvice(basePackages = "com.campushelp")
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> t = returnType.getParameterType();
        if (ApiResult.class.isAssignableFrom(t)) {
            return false;
        }
        if (org.springframework.http.ResponseEntity.class.isAssignableFrom(t)) {
            return false;
        }
        if (String.class.equals(t)) {
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {
        if (body instanceof ApiResult) {
            fillRequestId((ApiResult<?>) body);
            return body;
        }
        return ApiResult.ok(body, RequestIdAccessor.currentRequestId());
    }

    private static void fillRequestId(ApiResult<?> r) {
        if (r.getRequestId() == null) {
            r.setRequestId(RequestIdAccessor.currentRequestId());
        }
    }
}
