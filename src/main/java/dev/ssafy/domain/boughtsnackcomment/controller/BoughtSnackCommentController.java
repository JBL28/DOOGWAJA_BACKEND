package dev.ssafy.domain.boughtsnackcomment.controller;

import dev.ssafy.domain.boughtsnackcomment.dto.BoughtSnackCommentDto;
import dev.ssafy.domain.boughtsnackcomment.service.BoughtSnackCommentService;
import dev.ssafy.global.response.ApiResponse;
import dev.ssafy.global.response.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bought-snacks/{구매_id}/comments")
@RequiredArgsConstructor
public class BoughtSnackCommentController {

    private final BoughtSnackCommentService commentService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<BoughtSnackCommentDto.Response>>> getComments(
            @PathVariable("구매_id") Long purchaseId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {

        return ResponseEntity.ok(ApiResponse.success(commentService.getComments(purchaseId, page, pageSize)));
    }

    @GetMapping("/{Key}")
    public ResponseEntity<ApiResponse<BoughtSnackCommentDto.Response>> getCommentDetail(
            @PathVariable("구매_id") Long purchaseId,
            @PathVariable("Key") Long commentId) {

        return ResponseEntity.ok(ApiResponse.success(commentService.getCommentDetail(purchaseId, commentId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BoughtSnackCommentDto.Response>> createComment(
            @PathVariable("구매_id") Long purchaseId,
            @Valid @RequestBody BoughtSnackCommentDto.Request request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(commentService.createComment(purchaseId, request), "댓글이 작성되었습니다"));
    }

    @PutMapping("/{Key}")
    public ResponseEntity<ApiResponse<BoughtSnackCommentDto.Response>> updateComment(
            @PathVariable("구매_id") Long purchaseId,
            @PathVariable("Key") Long commentId,
            @Valid @RequestBody BoughtSnackCommentDto.Request request) {

        return ResponseEntity.ok(ApiResponse.success(commentService.updateComment(purchaseId, commentId, request), "댓글이 수정되었습니다"));
    }

    @DeleteMapping("/{Key}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable("구매_id") Long purchaseId,
            @PathVariable("Key") Long commentId) {

        commentService.deleteComment(purchaseId, commentId);
        return ResponseEntity.ok(ApiResponse.message("댓글이 삭제되었습니다"));
    }
}
