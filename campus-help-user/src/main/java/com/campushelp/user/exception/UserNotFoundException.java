package com.campushelp.user.exception;

/**
 * 用户不存在（HTTP 404）。
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
