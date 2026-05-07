package com.campushelp.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * 统一响应体（V3）：与 {@link com.campushelp.common.web.ApiResponseBodyAdvice} 配合；
 * 异常处理可直接返回 {@code ResponseEntity.status(http).body(ApiResult.fail(...))}。
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {

    private boolean success;
    private String code;
    private String message;
    private T data;
    /** 毫秒时间戳 */
    private long timestamp;
    /** 与请求/响应头 X-Request-Id 一致，便于链路排查 */
    private String requestId;
    /** API 主版本号 */
    private String apiVersion = "3";

    public static <T> ApiResult<T> ok(T data) {
        return ok(data, null);
    }

    public static <T> ApiResult<T> ok(T data, String requestId) {
        ApiResult<T> r = new ApiResult<>();
        r.setSuccess(true);
        r.setCode(ResultCode.SUCCESS.getCode());
        r.setMessage(ResultCode.SUCCESS.getDefaultMessage());
        r.setData(data);
        r.setTimestamp(System.currentTimeMillis());
        r.setRequestId(requestId);
        return r;
    }

    public static <T> ApiResult<T> fail(ResultCode resultCode, String message) {
        return fail(resultCode, message, null);
    }

    public static <T> ApiResult<T> fail(ResultCode resultCode, String message, String requestId) {
        ApiResult<T> r = new ApiResult<>();
        r.setSuccess(false);
        r.setCode(resultCode.getCode());
        r.setMessage(message != null && !message.isEmpty() ? message : resultCode.getDefaultMessage());
        r.setData(null);
        r.setTimestamp(System.currentTimeMillis());
        r.setRequestId(requestId);
        return r;
    }
}
