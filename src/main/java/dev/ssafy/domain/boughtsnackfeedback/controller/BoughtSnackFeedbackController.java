package dev.ssafy.domain.boughtsnackfeedback.controller;

import dev.ssafy.domain.boughtsnackfeedback.dto.BoughtSnackFeedbackDto;
import dev.ssafy.domain.boughtsnackfeedback.entity.BoughtSnackFeedbackEntity.FeedbackReaction;
import dev.ssafy.domain.boughtsnackfeedback.service.BoughtSnackFeedbackService;
import dev.ssafy.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bought-snacks/{구매_id}")
@RequiredArgsConstructor
public class BoughtSnackFeedbackController {

    private final BoughtSnackFeedbackService feedbackService;

    @PostMapping("/like")
    public ResponseEntity<ApiResponse<Void>> like(@PathVariable("구매_id") Long purchaseId) {
        feedbackService.toggleReaction(purchaseId, FeedbackReaction.LIKE);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.message("좋아요가 추가되었습니다"));
    }

    @DeleteMapping("/like")
    public ResponseEntity<ApiResponse<Void>> unlike(@PathVariable("구매_id") Long purchaseId) {
        feedbackService.removeReaction(purchaseId, FeedbackReaction.LIKE);
        return ResponseEntity.ok(ApiResponse.message("좋아요가 취소되었습니다"));
    }

    @PostMapping("/dislike")
    public ResponseEntity<ApiResponse<Void>> dislike(@PathVariable("구매_id") Long purchaseId) {
        feedbackService.toggleReaction(purchaseId, FeedbackReaction.DISLIKE);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.message("싫어요가 추가되었습니다"));
    }

    @DeleteMapping("/dislike")
    public ResponseEntity<ApiResponse<Void>> undislike(@PathVariable("구매_id") Long purchaseId) {
        feedbackService.removeReaction(purchaseId, FeedbackReaction.DISLIKE);
        return ResponseEntity.ok(ApiResponse.message("싫어요가 취소되었습니다"));
    }

    @GetMapping("/feedback-stats")
    public ResponseEntity<ApiResponse<BoughtSnackFeedbackDto.StatsResponse>> getStats(@PathVariable("구매_id") Long purchaseId) {
        return ResponseEntity.ok(ApiResponse.success(feedbackService.getStats(purchaseId)));
    }
}
