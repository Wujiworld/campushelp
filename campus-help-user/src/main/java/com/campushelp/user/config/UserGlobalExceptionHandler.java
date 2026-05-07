package com.campushelp.user.config;

import com.campushelp.common.api.ApiResult;
import com.campushelp.common.api.ResultCode;
import com.campushelp.common.web.RequestIdAccessor;
import com.campushelp.user.exception.BadRequestException;
import com.campushelp.user.exception.UnauthorizedException;
import com.campushelp.user.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 统一 {@link ApiResult} 错误体（V3）。
 */
@RestControllerAdvice(basePackages = "com.campushelp.user")
public class UserGlobalExceptionHandler {

    private static String rid() {
        return RequestIdAccessor.currentRequestId();
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResult<Void>> userNotFound(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResult.fail(ResultCode.NOT_FOUND, e.getMessage(), rid()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResult<Void>> badRequest(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResult.fail(ResultCode.CONFLICT, e.getMessage(), rid()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResult<Void>> unauthorized(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResult.fail(ResultCode.UNAUTHORIZED, e.getMessage(), rid()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> valid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(ResultCode.PARAM_INVALID, msg, rid()));
    }
}
