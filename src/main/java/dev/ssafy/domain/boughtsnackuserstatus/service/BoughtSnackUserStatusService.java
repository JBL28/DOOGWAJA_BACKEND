package dev.ssafy.domain.boughtsnackuserstatus.service;

import dev.ssafy.domain.boughtsnack.entity.BoughtSnackEntity;
import dev.ssafy.domain.boughtsnack.entity.BoughtSnackStatusEnum;
import dev.ssafy.domain.boughtsnack.repository.BoughtSnackRepository;
import dev.ssafy.domain.boughtsnackuserstatus.dto.BoughtSnackUserStatusDto;
import dev.ssafy.domain.boughtsnackuserstatus.entity.BoughtSnackUserStatusEntity;
import dev.ssafy.domain.boughtsnackuserstatus.repository.BoughtSnackUserStatusRepository;
import dev.ssafy.domain.user.entity.UserEntity;
import dev.ssafy.domain.user.repository.UserRepository;
import dev.ssafy.global.exception.BusinessException;
import dev.ssafy.global.exception.ErrorCode;
import dev.ssafy.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoughtSnackUserStatusService {

    private final BoughtSnackUserStatusRepository userStatusRepository;
    private final BoughtSnackRepository boughtSnackRepository;
    private final UserRepository userRepository;

    @Transactional
    public void updateStatus(Long purchaseId, BoughtSnackStatusEnum status) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        BoughtSnackEntity boughtSnack = boughtSnackRepository.findById(purchaseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "구매 과자를 찾을 수 없습니다"));

        Optional<BoughtSnackUserStatusEntity> existingStatusOpt = userStatusRepository.findByBoughtSnackIdAndUserId(purchaseId, user.getId());

        if (existingStatusOpt.isPresent()) {
            existingStatusOpt.get().updateStatus(status);
        } else {
            BoughtSnackUserStatusEntity newStatus = BoughtSnackUserStatusEntity.builder()
                    .boughtSnack(boughtSnack)
                    .user(user)
                    .status(status)
                    .build();
            userStatusRepository.save(newStatus);
        }
    }

    public BoughtSnackUserStatusDto.Response getMyStatus(Long purchaseId) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!boughtSnackRepository.existsById(purchaseId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "구매 과자를 찾을 수 없습니다");
        }

        BoughtSnackStatusEnum status = userStatusRepository.findByBoughtSnackIdAndUserId(purchaseId, user.getId())
                .map(BoughtSnackUserStatusEntity::getStatus).orElse(null);

        return BoughtSnackUserStatusDto.Response.of(purchaseId, status);
    }
}
