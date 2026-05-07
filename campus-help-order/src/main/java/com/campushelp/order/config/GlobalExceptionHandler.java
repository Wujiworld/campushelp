package com.campushelp.order.config;

import com.campushelp.common.api.ApiResult;
import com.campushelp.common.api.ResultCode;
import com.campushelp.common.web.RequestIdAccessor;
import com.campushelp.order.exception.BadRequestException;
import com.campushelp.order.exception.OrderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 异常映射为 {@link ApiResult} + 对应 HTTP 状态码（V3）。
 */
@RestControllerAdvice(basePackages = "com.campushelp.order")
public class GlobalExceptionHandler {

    private static String rid() {
        return RequestIdAccessor.currentRequestId();
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResult<Void>> notFound(OrderNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResult.fail(ResultCode.NOT_FOUND, e.getMessage(), rid()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResult<Void>> badRequest(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResult.fail(ResultCode.BIZ_RULE, e.getMessage(), rid()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResult<Void>> illegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResult.fail(ResultCode.UNAUTHORIZED, e.getMessage(), rid()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResult<Void>> valid(Exception e) {
        String msg;
        if (e instanceof MethodArgumentNotValidException) {
            msg = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        } else {
            msg = ((BindException) e).getBindingResult().getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(ResultCode.PARAM_INVALID, msg, rid()));
    }
}
