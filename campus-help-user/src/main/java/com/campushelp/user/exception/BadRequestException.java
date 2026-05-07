package com.campushelp.user.exception;

/**
 * 业务校验失败（如手机号已注册、密码错误）。
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
