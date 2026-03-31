package dev.ssafy.domain.recommendation.controller;

import dev.ssafy.domain.recommendation.dto.RecommendationDto;
import dev.ssafy.domain.recommendation.service.RecommendationService;
import dev.ssafy.global.response.ApiResponse;
import dev.ssafy.global.response.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<RecommendationDto.Response>>> getRecommendations(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {

        return ResponseEntity.ok(ApiResponse.success(recommendationService.getRecommendations(page, pageSize)));
    }

    @GetMapping("/{주문_id}")
    public ResponseEntity<ApiResponse<RecommendationDto.Response>> getRecommendationDetail(
            @PathVariable("주문_id") Long orderId) {
            
        return ResponseEntity.ok(ApiResponse.success(recommendationService.getRecommendationDetail(orderId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecommendationDto.Response>> createRecommendation(
            @Valid @RequestBody RecommendationDto.Request request) {
            
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(recommendationService.createRecommendation(request), "추천 게시물이 작성되었습니다"));
    }

    @PutMapping("/{주문_id}")
    public ResponseEntity<ApiResponse<RecommendationDto.Response>> updateRecommendation(
            @PathVariable("주문_id") Long orderId,
            @Valid @RequestBody RecommendationDto.UpdateRequest request) {
            
        return ResponseEntity.ok(ApiResponse.success(recommendationService.updateRecommendation(orderId, request), "추천 게시물이 수정되었습니다"));
    }

    @DeleteMapping("/{주문_id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecommendation(
            @PathVariable("주문_id") Long orderId) {
            
        recommendationService.deleteRecommendation(orderId);
        return ResponseEntity.ok(ApiResponse.message("추천 게시물이 삭제되었습니다"));
    }
}
