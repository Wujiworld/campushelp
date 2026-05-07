package com.campushelp.common.exception;

import com.campushelp.common.api.ResultCode;

/**
 * 资源不存在异常：映射为 HTTP 404。
 */
public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(ResultCode.NOT_FOUND, message);
    }

    public NotFoundException(String resourceName, Object resourceId) {
        super(ResultCode.NOT_FOUND, resourceName + " 不存在: " + resourceId);
    }
}
