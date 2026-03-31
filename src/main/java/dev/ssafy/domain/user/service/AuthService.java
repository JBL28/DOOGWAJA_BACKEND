package dev.ssafy.domain.user.service;

import dev.ssafy.domain.user.dto.AuthDto;
import dev.ssafy.domain.user.dto.UserDto;
import dev.ssafy.domain.user.entity.UserEntity;
import dev.ssafy.domain.user.entity.UserRole;
import dev.ssafy.domain.user.repository.UserRepository;
import dev.ssafy.global.exception.BusinessException;
import dev.ssafy.global.exception.ErrorCode;
import dev.ssafy.global.security.JwtTokenProvider;
import dev.ssafy.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthDto.TokenResponse signup(AuthDto.SignupRequest request) {
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        UserEntity user = UserEntity.builder()
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER) // 기본 권한
                .build();

        userRepository.save(user);

        return generateTokenResponse(user);
    }

    @Transactional
    public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
        UserEntity user = userRepository.findByNickname(request.getNickname())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return generateTokenResponse(user);
    }

    @Transactional
    public void logout() {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        user.removeRefreshToken();
    }

    @Transactional
    public AuthDto.RefreshTokenResponse refreshToken(AuthDto.RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        UserEntity user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_EXPIRED, "존재하지 않는 리프레시 토큰입니다."));

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname(), user.getRole().getValue());

        return new AuthDto.RefreshTokenResponse(newAccessToken);
    }

    @Transactional
    public void changePassword(AuthDto.ChangePasswordRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "현재 비밀번호가 일치하지 않습니다");
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    private AuthDto.TokenResponse generateTokenResponse(UserEntity user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname(), user.getRole().getValue());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getNickname());

        user.updateRefreshToken(refreshToken);

        return new AuthDto.TokenResponse(accessToken, refreshToken, UserDto.from(user));
    }
}
