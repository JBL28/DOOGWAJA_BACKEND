package dev.ssafy.global.response;

import lombok.Getter;

@Getter
public class ErrorResponse {

    private final boolean success = false;
    private final String message;
    private final String errorCode;

    public ErrorResponse(String message, String errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }
}
