package dev.ssafy.domain.recommendationfeedback.controller;

import dev.ssafy.domain.recommendationfeedback.dto.RecommendationFeedbackDto;
import dev.ssafy.domain.recommendationfeedback.entity.RecommendationFeedbackEntity.FeedbackReaction;
import dev.ssafy.domain.recommendationfeedback.service.RecommendationFeedbackService;
import dev.ssafy.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations/{주문_id}")
@RequiredArgsConstructor
public class RecommendationFeedbackController {

    private final RecommendationFeedbackService feedbackService;

    @PostMapping("/like")
    public ResponseEntity<ApiResponse<Void>> like(@PathVariable("주문_id") Long orderId) {
        feedbackService.toggleReaction(orderId, FeedbackReaction.LIKE);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.message("좋아요가 추가되었습니다"));
    }

    @DeleteMapping("/like")
    public ResponseEntity<ApiResponse<Void>> unlike(@PathVariable("주문_id") Long orderId) {
        feedbackService.removeReaction(orderId, FeedbackReaction.LIKE);
        return ResponseEntity.ok(ApiResponse.message("좋아요가 취소되었습니다"));
    }

    @PostMapping("/dislike")
    public ResponseEntity<ApiResponse<Void>> dislike(@PathVariable("주문_id") Long orderId) {
        feedbackService.toggleReaction(orderId, FeedbackReaction.DISLIKE);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.message("싫어요가 추가되었습니다"));
    }

    @DeleteMapping("/dislike")
    public ResponseEntity<ApiResponse<Void>> undislike(@PathVariable("주문_id") Long orderId) {
        feedbackService.removeReaction(orderId, FeedbackReaction.DISLIKE);
        return ResponseEntity.ok(ApiResponse.message("싫어요가 취소되었습니다"));
    }

    @GetMapping("/feedback-stats")
    public ResponseEntity<ApiResponse<RecommendationFeedbackDto.StatsResponse>> getStats(@PathVariable("주문_id") Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(feedbackService.getStats(orderId)));
    }
}
