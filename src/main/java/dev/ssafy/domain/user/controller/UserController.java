package dev.ssafy.domain.user.controller;

import dev.ssafy.domain.user.dto.UserDto;
import dev.ssafy.domain.user.entity.UserEntity;
import dev.ssafy.domain.user.repository.UserRepository;
import dev.ssafy.global.exception.BusinessException;
import dev.ssafy.global.exception.ErrorCode;
import dev.ssafy.global.response.ApiResponse;
import dev.ssafy.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final dev.ssafy.domain.user.service.UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getMyInfo() {
        return ResponseEntity.ok(ApiResponse.success(userService.getMyInfo()));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> updateMyInfo(@Valid @RequestBody UserDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateMyInfo(request), "프로필이 수정되었습니다."));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMyAccount() {
        userService.deleteMyAccount();
        return ResponseEntity.ok(ApiResponse.message("계정이 성공적으로 삭제되었습니다."));
    }
}
