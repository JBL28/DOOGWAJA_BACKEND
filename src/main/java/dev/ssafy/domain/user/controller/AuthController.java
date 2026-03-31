package dev.ssafy.domain.user.controller;

import dev.ssafy.domain.user.dto.AuthDto;
import dev.ssafy.domain.user.service.AuthService;
import dev.ssafy.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthDto.TokenResponse>> signup(@Valid @RequestBody AuthDto.SignupRequest request) {
        AuthDto.TokenResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "회원가입 성공"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.TokenResponse>> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        AuthDto.TokenResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "로그인 성공"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.message("로그아웃되었습니다"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthDto.RefreshTokenResponse>> refreshToken(@Valid @RequestBody AuthDto.RefreshTokenRequest request) {
        AuthDto.RefreshTokenResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "토큰이 갱신되었습니다"));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody AuthDto.ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.message("비밀번호가 변경되었습니다"));
    }
}
