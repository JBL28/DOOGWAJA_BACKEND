package dev.ssafy.domain.boughtsnack.dto;

import dev.ssafy.domain.boughtsnack.entity.BoughtSnackEntity;
import dev.ssafy.domain.boughtsnack.entity.BoughtSnackStatusEnum;
import dev.ssafy.domain.boughtsnackfeedback.entity.BoughtSnackFeedbackEntity.FeedbackReaction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

public class BoughtSnackDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @NotBlank(message = "과자이름은 필수입니다")
        @Size(max = 100, message = "과자이름은 최대 100자입니다")
        private String 과자이름;

        private BoughtSnackStatusEnum 상태;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class UpdateRequest {
        @Size(max = 100, message = "과자이름은 최대 100자입니다")
        private String 과자이름;

        private BoughtSnackStatusEnum 상태;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class UpdateStatusRequest {
        private BoughtSnackStatusEnum 상태;
    }

    @Getter
    public static class Response {
        private Long 구매_id;
        private String 과자이름;
        private String 상태;

        private long commentCount;
        private long likeCount;
        private long dislikeCount;
        private String myFeedback;
        private String myStatus;

        private String createdAt;
        private String updatedAt;

        @Builder
        public Response(Long 구매_id, String 과자이름, String 상태, long commentCount,
                        long likeCount, long dislikeCount, String myFeedback, String myStatus,
                        String createdAt, String updatedAt) {
            this.구매_id = 구매_id;
            this.과자이름 = 과자이름;
            this.상태 = 상태;
            this.commentCount = commentCount;
            this.likeCount = likeCount;
            this.dislikeCount = dislikeCount;
            this.myFeedback = myFeedback;
            this.myStatus = myStatus;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public static Response of(BoughtSnackEntity entity, long commentCount,
                                  long likeCount, long dislikeCount,
                                  FeedbackReaction myFeedback, BoughtSnackStatusEnum myStatus) {
            return Response.builder()
                    .구매_id(entity.getId())
                    .과자이름(entity.getSnackName())
                    .상태(entity.getStatus().getValue())
                    .commentCount(commentCount)
                    .likeCount(likeCount)
                    .dislikeCount(dislikeCount)
                    .myFeedback(myFeedback != null ? myFeedback.name() : null)
                    .myStatus(myStatus != null ? myStatus.getValue() : null)
                    .createdAt(entity.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .updatedAt(entity.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
        }
    }
}
