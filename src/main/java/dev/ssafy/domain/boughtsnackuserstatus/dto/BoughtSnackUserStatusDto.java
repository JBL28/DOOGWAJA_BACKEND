package dev.ssafy.domain.boughtsnackuserstatus.dto;

import dev.ssafy.domain.boughtsnack.entity.BoughtSnackStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BoughtSnackUserStatusDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Request {
        @NotNull(message = "상태값은 필수입니다")
        private BoughtSnackStatusEnum 상태;
    }

    @Getter
    public static class Response {
        private Long 구매_id;
        private String myStatus;

        @Builder
        public Response(Long 구매_id, String myStatus) {
            this.구매_id = 구매_id;
            this.myStatus = myStatus;
        }

        public static Response of(Long purchaseId, BoughtSnackStatusEnum myStatus) {
            return Response.builder()
                    .구매_id(purchaseId)
                    .myStatus(myStatus != null ? myStatus.getValue() : null)
                    .build();
        }
    }
}
