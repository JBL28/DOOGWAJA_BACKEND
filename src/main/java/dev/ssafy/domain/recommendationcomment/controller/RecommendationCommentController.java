package dev.ssafy.domain.recommendationcomment.controller;

import dev.ssafy.domain.recommendationcomment.dto.RecommendationCommentDto;
import dev.ssafy.domain.recommendationcomment.service.RecommendationCommentService;
import dev.ssafy.global.response.ApiResponse;
import dev.ssafy.global.response.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations/{주문_id}/comments")
@RequiredArgsConstructor
public class RecommendationCommentController {

    private final RecommendationCommentService commentService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<RecommendationCommentDto.Response>>> getComments(
            @PathVariable("주문_id") Long orderId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {

        return ResponseEntity.ok(ApiResponse.success(commentService.getComments(orderId, page, pageSize)));
    }

    @GetMapping("/{댓글_id}")
    public ResponseEntity<ApiResponse<RecommendationCommentDto.Response>> getCommentDetail(
            @PathVariable("주문_id") Long orderId,
            @PathVariable("댓글_id") Long commentId) {

        return ResponseEntity.ok(ApiResponse.success(commentService.getCommentDetail(orderId, commentId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecommendationCommentDto.Response>> createComment(
            @PathVariable("주문_id") Long orderId,
            @Valid @RequestBody RecommendationCommentDto.Request request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(commentService.createComment(orderId, request), "예상하는 응답"));
    }

    @PutMapping("/{댓글_id}")
    public ResponseEntity<ApiResponse<RecommendationCommentDto.Response>> updateComment(
            @PathVariable("주문_id") Long orderId,
            @PathVariable("댓글_id") Long commentId,
            @Valid @RequestBody RecommendationCommentDto.Request request) {

        return ResponseEntity.ok(ApiResponse.success(commentService.updateComment(orderId, commentId, request), "댓글이 수정되었습니다"));
    }

    @DeleteMapping("/{댓글_id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable("주문_id") Long orderId,
            @PathVariable("댓글_id") Long commentId) {

        commentService.deleteComment(orderId, commentId);
        return ResponseEntity.ok(ApiResponse.message("댓글이 삭제되었습니다"));
    }
}
