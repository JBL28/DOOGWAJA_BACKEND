package dev.ssafy.domain.boughtsnackfeedback.service;

import dev.ssafy.domain.boughtsnack.entity.BoughtSnackEntity;
import dev.ssafy.domain.boughtsnack.repository.BoughtSnackRepository;
import dev.ssafy.domain.boughtsnackfeedback.dto.BoughtSnackFeedbackDto;
import dev.ssafy.domain.boughtsnackfeedback.entity.BoughtSnackFeedbackEntity;
import dev.ssafy.domain.boughtsnackfeedback.entity.BoughtSnackFeedbackEntity.FeedbackReaction;
import dev.ssafy.domain.boughtsnackfeedback.repository.BoughtSnackFeedbackRepository;
import dev.ssafy.domain.user.entity.UserEntity;
import dev.ssafy.domain.user.repository.UserRepository;
import dev.ssafy.global.exception.BusinessException;
import dev.ssafy.global.exception.ErrorCode;
import dev.ssafy.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoughtSnackFeedbackService {

    private final BoughtSnackFeedbackRepository feedbackRepository;
    private final BoughtSnackRepository boughtSnackRepository;
    private final UserRepository userRepository;

    @Transactional
    public void toggleReaction(Long purchaseId, FeedbackReaction newReaction) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        BoughtSnackEntity boughtSnack = boughtSnackRepository.findById(purchaseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "구매 과자를 찾을 수 없습니다"));

        Optional<BoughtSnackFeedbackEntity> existingFeedbackOpt = feedbackRepository.findByBoughtSnackIdAndUserId(purchaseId, user.getId());

        if (existingFeedbackOpt.isPresent()) {
            BoughtSnackFeedbackEntity existingFeedback = existingFeedbackOpt.get();
            if (existingFeedback.getReaction() == newReaction) {
                // 이미 동일한 반응이면 무시 (idempotent)
                return;
            }
            // 다른 반응이면 업데이트
            existingFeedback.updateReaction(newReaction);
        } else {
            // 새로운 반응
            BoughtSnackFeedbackEntity newFeedbackEntity = BoughtSnackFeedbackEntity.builder()
                    .boughtSnack(boughtSnack)
                    .user(user)
                    .reaction(newReaction)
                    .build();
            feedbackRepository.save(newFeedbackEntity);
        }
    }

    @Transactional
    public void removeReaction(Long purchaseId, FeedbackReaction reactionToRemove) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!boughtSnackRepository.existsById(purchaseId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "구매 과자를 찾을 수 없습니다");
        }

        Optional<BoughtSnackFeedbackEntity> existingFeedbackOpt = feedbackRepository.findByBoughtSnackIdAndUserId(purchaseId, user.getId());

        if (existingFeedbackOpt.isPresent()) {
            BoughtSnackFeedbackEntity existingFeedback = existingFeedbackOpt.get();
            if (existingFeedback.getReaction() == reactionToRemove) {
                feedbackRepository.delete(existingFeedback);
            }
            // 요청한 reaction과 다르면 무시
        }
    }

    public BoughtSnackFeedbackDto.StatsResponse getStats(Long purchaseId) {
        if (!boughtSnackRepository.existsById(purchaseId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "구매 과자를 찾을 수 없습니다");
        }

        Map<FeedbackReaction, Long> counts = new HashMap<>();
        feedbackRepository.countFeedbackByPurchaseIds(List.of(purchaseId)).forEach(row -> {
            counts.put((FeedbackReaction) row[1], (Long) row[2]);
        });
        
        long likeCount = counts.getOrDefault(FeedbackReaction.LIKE, 0L);
        long dislikeCount = counts.getOrDefault(FeedbackReaction.DISLIKE, 0L);

        FeedbackReaction myFeedback = null;
        Optional<Long> currentUserIdOpt = SecurityUtil.getCurrentUserId();
        if (currentUserIdOpt.isPresent()) {
            UserEntity user = userRepository.findById(currentUserIdOpt.get()).orElse(null);
            if (user != null) {
                myFeedback = feedbackRepository.findByBoughtSnackIdAndUserId(purchaseId, user.getId())
                        .map(BoughtSnackFeedbackEntity::getReaction).orElse(null);
            }
        }

        return BoughtSnackFeedbackDto.StatsResponse.of(purchaseId, likeCount, dislikeCount, myFeedback);
    }
}
