package com.campushelp.common.exception;

import com.campushelp.common.api.ResultCode;

/**
 * 业务冲突异常：映射为 HTTP 409。
 * <p>
 * 例如：手机号已注册、重复提交等幂等性冲突。
 */
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(ResultCode.CONFLICT, message);
    }
}
