package com.campushelp.common.web;

import com.campushelp.common.api.ApiResult;
import com.campushelp.common.api.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一兜底处理未被各模块显式捕获的异常，避免返回不一致的错误体。
 */
@RestControllerAdvice(basePackages = "com.campushelp")
public class GlobalRestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalRestExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> fallback(Exception e) {
        String requestId = RequestIdAccessor.currentRequestId();
        log.error("Unhandled exception, requestId={}", requestId, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.fail(ResultCode.INTERNAL_ERROR, ResultCode.INTERNAL_ERROR.getDefaultMessage(), requestId));
    }
}
