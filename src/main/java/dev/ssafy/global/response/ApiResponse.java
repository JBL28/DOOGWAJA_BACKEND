package dev.ssafy.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success = true;
    private final T data;
    private final String message;

    private ApiResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, "요청이 성공했습니다");
    }

    public static ApiResponse<Void> message(String message) {
        return new ApiResponse<>(null, message);
    }
}
