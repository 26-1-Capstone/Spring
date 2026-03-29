package com.nutrishare.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    PERMISSION_DENIED("PERMISSION_DENIED", HttpStatus.FORBIDDEN, "권한이 없습니다."),
    INVALID_REQUEST("INVALID_REQUEST", HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류"),

    // GroupBuying
    GROUP_CLOSED("GROUP_CLOSED", HttpStatus.CONFLICT, "이미 마감된 공동구매입니다."),
    GROUP_ALREADY_JOINED("GROUP_ALREADY_JOINED", HttpStatus.CONFLICT, "이미 참여한 공동구매입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
