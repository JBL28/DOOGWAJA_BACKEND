package dev.ssafy.domain.recommendation.dto;

import dev.ssafy.domain.recommendation.entity.RecommendationEntity;
import dev.ssafy.domain.user.dto.UserDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

public class RecommendationDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @NotBlank(message = "과자이름은 필수입니다")
        @Size(min = 1, max = 100, message = "과자이름은 1자 이상 100자 이하이어야 합니다")
        private String 과자이름;

        @NotBlank(message = "주문이유는 필수입니다")
        @Size(min = 10, max = 1000, message = "주문이유는 최소 10자 이상이어야 합니다")
        private String 주문이유;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class UpdateRequest {
        private String 과자이름;

        @Size(min = 10, max = 1000, message = "주문이유는 최소 10자 이상이어야 합니다")
        private String 주문이유;
    }

    @Getter
    public static class Response {
        private Long 주문_id;
        private Long rc_id;
        private Long 사용자Id;
        private String 과자이름;
        private String 주문이유;
        private UserDto author;
        
        private long commentCount;
        private long likeCount;     // 프론트 스펙 호환용 (항상 0)
        private long dislikeCount;  // 프론트 스펙 호환용 (항상 0)
        private String myFeedback;  // 프론트 스펙 호환용 (항상 null)

        private String createdAt;
        private String updatedAt;

        @Builder
        public Response(Long 주문_id, Long rc_id, Long 사용자Id, String 과자이름, String 주문이유, 
                        UserDto author, long commentCount, long likeCount, long dislikeCount, String myFeedback, String createdAt, String updatedAt) {
            this.주문_id = 주문_id;
            this.rc_id = rc_id;
            this.사용자Id = 사용자Id;
            this.과자이름 = 과자이름;
            this.주문이유 = 주문이유;
            this.author = author;
            this.commentCount = commentCount;
            this.likeCount = likeCount;
            this.dislikeCount = dislikeCount;
            this.myFeedback = myFeedback;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public static Response of(RecommendationEntity entity, UserDto authorDto, long commentCount, long likeCount, long dislikeCount, dev.ssafy.domain.recommendationfeedback.entity.RecommendationFeedbackEntity.FeedbackReaction myFeedback) {
            return Response.builder()
                    .주문_id(entity.getId())
                    .rc_id(entity.getRcId())
                    .사용자Id(entity.getAuthor().getId())
                    .과자이름(entity.getSnackName())
                    .주문이유(entity.getReason())
                    .author(authorDto)
                    .commentCount(commentCount)
                    .likeCount(likeCount)
                    .dislikeCount(dislikeCount)
                    .myFeedback(myFeedback != null ? myFeedback.name() : null)
                    .createdAt(entity.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .updatedAt(entity.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
        }
    }
}
