package dev.ssafy.domain.boughtsnackcomment.dto;

import dev.ssafy.domain.boughtsnackcomment.entity.BoughtSnackCommentEntity;
import dev.ssafy.domain.boughtsnackfeedback.entity.BoughtSnackFeedbackEntity.FeedbackReaction;
import dev.ssafy.domain.user.dto.UserDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

public class BoughtSnackCommentDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @NotBlank(message = "내용은 필수입니다")
        @Size(max = 500, message = "내용은 500자 이하여야 합니다")
        private String 내용;
    }

    @Getter
    public static class Response {
        private Long Key;
        private Long 구매_id;
        private Long 사용자Id;
        private String 내용;
        private UserDto author;

        private long likeCount;
        private long dislikeCount;
        private String myFeedback;

        private String createdAt;
        private String updatedAt;

        @Builder
        public Response(Long Key, Long 구매_id, Long 사용자Id, String 내용, UserDto author,
                        long likeCount, long dislikeCount, String myFeedback, String createdAt, String updatedAt) {
            this.Key = Key;
            this.구매_id = 구매_id;
            this.사용자Id = 사용자Id;
            this.내용 = 내용;
            this.author = author;
            this.likeCount = likeCount; // bought snack API의 comment에는 각 개별 댓글마다 like가 있지 않고 과자 자체에만 like가 있지만, 프론트 스펙에 포함되어 있음 -> 0 고정
            this.dislikeCount = dislikeCount;
            this.myFeedback = myFeedback;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public static Response of(BoughtSnackCommentEntity entity, UserDto authorDto, long likeCount, long dislikeCount, FeedbackReaction myFeedback) {
            return Response.builder()
                    .Key(entity.getId())
                    .구매_id(entity.getBoughtSnack().getId())
                    .사용자Id(entity.getAuthor().getId())
                    .내용(entity.getContent())
                    .author(authorDto)
                    .likeCount(likeCount)
                    .dislikeCount(dislikeCount)
                    .myFeedback(myFeedback != null ? myFeedback.name() : null)
                    .createdAt(entity.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .updatedAt(entity.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
        }
    }
}
