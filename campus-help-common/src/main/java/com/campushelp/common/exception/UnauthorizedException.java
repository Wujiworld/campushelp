package com.campushelp.common.exception;

import com.campushelp.common.api.ResultCode;

/**
 * 未授权异常：映射为 HTTP 401。
 * <p>
 * 例如：用户未登录、Token 无效、无权操作他人资源等。
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(ResultCode.UNAUTHORIZED, message);
    }
}
