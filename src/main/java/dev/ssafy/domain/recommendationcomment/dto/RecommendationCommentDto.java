package dev.ssafy.domain.recommendationcomment.dto;

import dev.ssafy.domain.recommendationcomment.entity.RecommendationCommentEntity;
import dev.ssafy.domain.recommendationcomment.entity.RecommendationCommentFeedbackEntity.FeedbackReaction;
import dev.ssafy.domain.user.dto.UserDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

public class RecommendationCommentDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @NotBlank(message = "내용은 필수입니다")
        @Size(max = 500, message = "내용은 500자 이하여야 합니다")
        private String 내용;
    }

    @Getter
    public static class Response {
        private Long 댓글_id;
        private Long 주문_id;
        private Long 사용자Id;
        private String 내용;
        private UserDto author;

        private long likeCount;
        private long dislikeCount;
        private String myFeedback;

        private String createdAt;
        private String updatedAt;

        @Builder
        public Response(Long 댓글_id, Long 주문_id, Long 사용자Id, String 내용, UserDto author,
                        long likeCount, long dislikeCount, String myFeedback, String createdAt, String updatedAt) {
            this.댓글_id = 댓글_id;
            this.주문_id = 주문_id;
            this.사용자Id = 사용자Id;
            this.내용 = 내용;
            this.author = author;
            this.likeCount = likeCount;
            this.dislikeCount = dislikeCount;
            this.myFeedback = myFeedback;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public static Response of(RecommendationCommentEntity entity, UserDto authorDto, long likeCount, long dislikeCount, FeedbackReaction myFeedback) {
            return Response.builder()
                    .댓글_id(entity.getId())
                    .주문_id(entity.getRecommendation().getId())
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
