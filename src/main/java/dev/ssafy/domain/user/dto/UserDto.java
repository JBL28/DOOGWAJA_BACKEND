package dev.ssafy.domain.user.dto;

import dev.ssafy.domain.user.entity.UserEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class UpdateRequest {
        @NotBlank(message = "닉네임을 입력해주세요")
        @Size(min=2, max=20, message = "닉네임은 2~20자 사이여야 합니다")
        private String nickname;
    }
    
    private Long user_id;
    private String nickname;
    private String role;
    private String createdAt;
    private String updatedAt;

    @Builder
    public UserDto(Long user_id, String nickname, String role, String createdAt, String updatedAt) {
        this.user_id = user_id;
        this.nickname = nickname;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserDto from(UserEntity user) {
        return UserDto.builder()
                .user_id(user.getId())
                .nickname(user.getNickname())
                .role(user.getRole().getValue().toLowerCase())
                .createdAt(user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) // ISO 8601 형식
                .updatedAt(user.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
}
