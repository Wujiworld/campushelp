package com.campushelp.user.exception;

/**
 * 登录失败（与注册类 400 区分，便于 HTTP 状态码对齐）。
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
