package dev.ssafy.domain.boughtsnackfeedback.dto;

import dev.ssafy.domain.boughtsnackfeedback.entity.BoughtSnackFeedbackEntity.FeedbackReaction;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BoughtSnackFeedbackDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class StatsResponse {
        private Long 구매_id;
        private long likeCount;
        private long dislikeCount;
        private String myFeedback;

        @Builder
        public StatsResponse(Long 구매_id, long likeCount, long dislikeCount, String myFeedback) {
            this.구매_id = 구매_id;
            this.likeCount = likeCount;
            this.dislikeCount = dislikeCount;
            this.myFeedback = myFeedback;
        }

        public static StatsResponse of(Long purchaseId, long likeCount, long dislikeCount, FeedbackReaction myFeedback) {
            return StatsResponse.builder()
                    .구매_id(purchaseId)
                    .likeCount(likeCount)
                    .dislikeCount(dislikeCount)
                    .myFeedback(myFeedback != null ? myFeedback.name() : null)
                    .build();
        }
    }
}
