package dev.ssafy.domain.recommendationfeedback.dto;

import lombok.Builder;
import lombok.Getter;

public class RecommendationFeedbackDto {

    @Getter
    public static class StatsResponse {
        private Long 주문_id;
        private long likeCount;
        private long dislikeCount;
        private String myFeedback;

        @Builder
        public StatsResponse(Long 주문_id, long likeCount, long dislikeCount, String myFeedback) {
            this.주문_id = 주문_id;
            this.likeCount = likeCount;
            this.dislikeCount = dislikeCount;
            this.myFeedback = myFeedback;
        }
    }
}
