package com.campushelp.common.web;

import com.campushelp.common.api.ApiResult;
import com.campushelp.common.api.ResultCode;
import com.campushelp.common.exception.BusinessException;
import com.campushelp.common.exception.ConflictException;
import com.campushelp.common.exception.NotFoundException;
import com.campushelp.common.exception.UnauthorizedException;
import com.campushelp.common.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 统一业务异常处理器：将 common.exception 下的标准异常映射为 HTTP 状态码 + ApiResult。
 * <p>
 * 优先级高于 {@link GlobalRestExceptionHandler}，但低于各模块自定义的 ExceptionHandler。
 */
@RestControllerAdvice(basePackages = "com.campushelp")
public class UnifiedExceptionHandler {

    private static String rid() {
        return RequestIdAccessor.currentRequestId();
    }

    /**
     * 资源不存在 → 404
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResult<Void>> notFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResult.fail(ResultCode.NOT_FOUND, e.getMessage(), rid()));
    }

    /**
     * 未授权 → 401
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResult<Void>> unauthorized(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResult.fail(ResultCode.UNAUTHORIZED, e.getMessage(), rid()));
    }

    /**
     * 业务冲突 → 409
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResult<Void>> conflict(ConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResult.fail(ResultCode.CONFLICT, e.getMessage(), rid()));
    }

    /**
     * 业务规则校验失败 → 422
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResult<Void>> validation(ValidationException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResult.fail(ResultCode.BIZ_RULE, e.getMessage(), rid()));
    }

    /**
     * 通用业务异常（兜底）→ 根据异常内的 ResultCode 映射
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> business(BusinessException e) {
        HttpStatus status = mapToHttpStatus(e.getResultCode());
        return ResponseEntity.status(status)
                .body(ApiResult.fail(e.getResultCode(), e.getMessage(), rid()));
    }

    /**
     * 参数校验失败（@Valid / @Validated）→ 400
     * <p>
     * 统一格式："{field}: {message}; {field}: {message}"
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResult<Void>> valid(Exception e) {
        String msg;
        if (e instanceof MethodArgumentNotValidException) {
            msg = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        } else {
            msg = ((BindException) e).getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("; "));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(ResultCode.PARAM_INVALID, msg, rid()));
    }

    /**
     * 将 ResultCode 映射为 HTTP 状态码
     */
    private static HttpStatus mapToHttpStatus(ResultCode code) {
        if (code == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        switch (code) {
            case NOT_FOUND:
                return HttpStatus.NOT_FOUND;
            case UNAUTHORIZED:
                return HttpStatus.UNAUTHORIZED;
            case FORBIDDEN:
                return HttpStatus.FORBIDDEN;
            case CONFLICT:
                return HttpStatus.CONFLICT;
            case BIZ_RULE:
                return HttpStatus.UNPROCESSABLE_ENTITY;
            case PARAM_INVALID:
                return HttpStatus.BAD_REQUEST;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
