package dev.ssafy.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // Auth
    AUTH_REQUIRED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다", "AUTH_REQUIRED"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "닉네임 또는 비밀번호가 잘못되었습니다", "INVALID_CREDENTIALS"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다", "TOKEN_EXPIRED"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다", "TOKEN_EXPIRED"),

    // Access
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다", "ACCESS_DENIED"),

    // Resource
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다", "NOT_FOUND"),

    // Conflict
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다", "DUPLICATE_NICKNAME"),

    // Validation
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다", "VALIDATION_ERROR"),

    // Server
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다", "SERVER_ERROR");

    private final HttpStatus status;
    private final String message;
    private final String code;

    ErrorCode(HttpStatus status, String message, String code) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public String getCode() { return code; }
}
