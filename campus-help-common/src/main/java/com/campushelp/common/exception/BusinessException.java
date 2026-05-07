package com.campushelp.common.exception;

import com.campushelp.common.api.ResultCode;
import lombok.Getter;

/**
 * 业务异常基类：携带业务错误码，便于统一映射为 HTTP 状态码与 ApiResult。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public BusinessException(ResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.resultCode = resultCode;
    }
}
