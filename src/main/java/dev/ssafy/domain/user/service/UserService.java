package dev.ssafy.domain.user.service;

import dev.ssafy.domain.user.dto.UserDto;
import dev.ssafy.domain.user.entity.UserEntity;
import dev.ssafy.domain.user.repository.UserRepository;
import dev.ssafy.global.exception.BusinessException;
import dev.ssafy.global.exception.ErrorCode;
import dev.ssafy.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserDto getMyInfo() {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED, "인증이 필요합니다."));

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        return UserDto.from(user);
    }

    @Transactional
    public UserDto updateMyInfo(UserDto.UpdateRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED, "인증이 필요합니다."));

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 닉네임 중복 체크 (수정 시)
        if (!user.getNickname().equals(request.getNickname()) && userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME, "이미 사용중인 닉네임입니다.");
        }

        user.updateNickname(request.getNickname());

        return UserDto.from(user);
    }

    @Transactional
    public void deleteMyAccount() {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED, "인증이 필요합니다."));

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        userRepository.delete(user);
    }
}
