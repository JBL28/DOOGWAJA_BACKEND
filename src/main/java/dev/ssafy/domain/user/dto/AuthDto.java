package dev.ssafy.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class LoginRequest {
        @NotBlank(message = "닉네임은 필수입니다")
        private String nickname;

        @NotBlank(message = "비밀번호는 필수입니다")
        private String password;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class SignupRequest {
        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하이어야 합니다")
        private String nickname;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        private String password;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RefreshTokenRequest {
        @NotBlank(message = "리프레시 토큰은 필수입니다")
        private String refreshToken;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ChangePasswordRequest {
        @NotBlank(message = "현재 비밀번호는 필수입니다")
        private String currentPassword;

        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Size(min = 8, message = "새 비밀번호는 최소 8자 이상이어야 합니다")
        private String newPassword;
    }

    @Getter
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
        private UserDto user;

        public TokenResponse(String accessToken, String refreshToken, UserDto user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.user = user;
        }
    }

    @Getter
    public static class RefreshTokenResponse {
        private String accessToken;

        public RefreshTokenResponse(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}
