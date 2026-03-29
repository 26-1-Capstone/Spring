package com.nutrishare.common.exception;

import com.nutrishare.common.api.ApiError;
import com.nutrishare.common.api.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(DomainException.class)
        public ResponseEntity<ApiResponse<Void>> handleDomainException(
                        DomainException e) {
                ErrorCode code = e.getErrorCode();
                return ResponseEntity
                                .status(code.getStatus())
                                .body(ApiResponse.error(
                                                new ApiError(code.getCode(), code.getMessage())));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Void>> handleValidationException(
                        MethodArgumentNotValidException e) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(
                                                new ApiError(
                                                                ErrorCode.INVALID_REQUEST.getCode(),
                                                                "요청 값이 올바르지 않습니다.")));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
                        Exception e) {
                log.error("Unexpected error", e);
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(
                                                new ApiError(
                                                                ErrorCode.INTERNAL_ERROR.getCode(),
                                                                ErrorCode.INTERNAL_ERROR.getMessage())));
        }

        @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(
                        org.springframework.web.servlet.resource.NoResourceFoundException e) {
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(
                                                new ApiError(
                                                                ErrorCode.NOT_FOUND.getCode(),
                                                                "리소스를 찾을 수 없습니다.")));
        }
}
