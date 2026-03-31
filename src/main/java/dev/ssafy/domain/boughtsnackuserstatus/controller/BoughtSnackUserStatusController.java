package dev.ssafy.domain.boughtsnackuserstatus.controller;

import dev.ssafy.domain.boughtsnackuserstatus.dto.BoughtSnackUserStatusDto;
import dev.ssafy.domain.boughtsnackuserstatus.service.BoughtSnackUserStatusService;
import dev.ssafy.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bought-snacks/{구매_id}")
@RequiredArgsConstructor
public class BoughtSnackUserStatusController {

    private final BoughtSnackUserStatusService userStatusService;

    @PutMapping("/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable("구매_id") Long purchaseId,
            @Valid @RequestBody BoughtSnackUserStatusDto.Request request) {

        userStatusService.updateStatus(purchaseId, request.get상태());
        return ResponseEntity.ok(ApiResponse.message("상태가 변경되었습니다"));
    }

    @GetMapping("/my-status")
    public ResponseEntity<ApiResponse<BoughtSnackUserStatusDto.Response>> getMyStatus(
            @PathVariable("구매_id") Long purchaseId) {

        return ResponseEntity.ok(ApiResponse.success(userStatusService.getMyStatus(purchaseId)));
    }
}
