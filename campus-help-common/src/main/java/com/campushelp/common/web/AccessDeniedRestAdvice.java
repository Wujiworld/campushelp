package com.campushelp.common.web;

import com.campushelp.common.api.ApiResult;
import com.campushelp.common.api.ResultCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 将 {@link AccessDeniedException}（角色不足等）映射为 403 + 统一体。
 */
@RestControllerAdvice
public class AccessDeniedRestAdvice {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResult<Void>> forbidden(AccessDeniedException e) {
        String rid = RequestIdAccessor.currentRequestId();
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResult.fail(ResultCode.FORBIDDEN,
                        e.getMessage() != null ? e.getMessage() : ResultCode.FORBIDDEN.getDefaultMessage(),
                        rid));
    }
}
