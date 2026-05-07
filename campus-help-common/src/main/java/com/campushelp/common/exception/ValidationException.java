package com.campushelp.common.exception;

import com.campushelp.common.api.ResultCode;

/**
 * 业务规则校验失败：映射为 HTTP 422。
 * <p>
 * 例如：库存不足、状态不允许操作、超过限购等。
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(ResultCode.BIZ_RULE, message);
    }
}
