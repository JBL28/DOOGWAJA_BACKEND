package dev.ssafy.domain.boughtsnack.controller;

import dev.ssafy.domain.boughtsnack.dto.BoughtSnackDto;
import dev.ssafy.domain.boughtsnack.entity.BoughtSnackStatusEnum;
import dev.ssafy.domain.boughtsnack.service.BoughtSnackService;
import dev.ssafy.global.response.ApiResponse;
import dev.ssafy.global.response.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bought-snacks")
@RequiredArgsConstructor
public class BoughtSnackController {

    private final BoughtSnackService boughtSnackService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<BoughtSnackDto.Response>>> getBoughtSnacks(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(name = "status", required = false) BoughtSnackStatusEnum status) {

        return ResponseEntity.ok(ApiResponse.success(boughtSnackService.getBoughtSnacks(page, pageSize, status)));
    }

    @GetMapping("/{구매_id}")
    public ResponseEntity<ApiResponse<BoughtSnackDto.Response>> getBoughtSnackDetail(
            @PathVariable("구매_id") Long purchaseId) {

        return ResponseEntity.ok(ApiResponse.success(boughtSnackService.getBoughtSnackDetail(purchaseId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BoughtSnackDto.Response>> createBoughtSnack(
            @Valid @RequestBody BoughtSnackDto.Request request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(boughtSnackService.createBoughtSnack(request), "구매 과자가 추가되었습니다"));
    }

    @PutMapping("/{구매_id}")
    public ResponseEntity<ApiResponse<BoughtSnackDto.Response>> updateBoughtSnack(
            @PathVariable("구매_id") Long purchaseId,
            @Valid @RequestBody BoughtSnackDto.UpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success(boughtSnackService.updateBoughtSnack(purchaseId, request), "구매 과자가 수정되었습니다"));
    }

    @DeleteMapping("/{구매_id}")
    public ResponseEntity<ApiResponse<Void>> deleteBoughtSnack(
            @PathVariable("구매_id") Long purchaseId) {

        boughtSnackService.deleteBoughtSnack(purchaseId);
        return ResponseEntity.ok(ApiResponse.message("구매 과자가 삭제되었습니다"));
    }
}
