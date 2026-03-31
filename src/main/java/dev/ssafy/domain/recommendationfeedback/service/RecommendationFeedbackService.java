package dev.ssafy.domain.recommendationfeedback.service;

import dev.ssafy.domain.recommendation.entity.RecommendationEntity;
import dev.ssafy.domain.recommendation.repository.RecommendationRepository;
import dev.ssafy.domain.recommendationfeedback.dto.RecommendationFeedbackDto;
import dev.ssafy.domain.recommendationfeedback.entity.RecommendationFeedbackEntity;
import dev.ssafy.domain.recommendationfeedback.entity.RecommendationFeedbackEntity.FeedbackReaction;
import dev.ssafy.domain.recommendationfeedback.repository.RecommendationFeedbackRepository;
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
public class RecommendationFeedbackService {

    private final RecommendationFeedbackRepository feedbackRepository;
    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void toggleReaction(Long orderId, FeedbackReaction reaction) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다"));

        RecommendationEntity rec = recommendationRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "추천 게시글을 찾을 수 없습니다"));

        Optional<RecommendationFeedbackEntity> existingOpt = feedbackRepository.findByRecommendationIdAndUserId(orderId, user.getId());

        if (existingOpt.isPresent()) {
            RecommendationFeedbackEntity existing = existingOpt.get();
            if (existing.getReaction() == reaction) {
                // 이미 동일한 반응이면 추가 무시 혹은 제거 (프론트 로직상 생성/삭제 API가 분리되어 있으므로 예외 혹은 무시)
                return;
            } else {
                // 다른 반응이면 업데이트
                existing.updateReaction(reaction);
            }
        } else {
            // 없으면 신규 생성
            RecommendationFeedbackEntity newFeedback = RecommendationFeedbackEntity.builder()
                    .recommendation(rec)
                    .user(user)
                    .reaction(reaction)
                    .build();
            feedbackRepository.save(newFeedback);
        }
    }

    @Transactional
    public void removeReaction(Long orderId, FeedbackReaction reaction) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다"));

        feedbackRepository.findByRecommendationIdAndUserId(orderId, user.getId())
                .ifPresent(existing -> {
                    if (existing.getReaction() == reaction) {
                        feedbackRepository.delete(existing);
                    }
                });
    }

    public RecommendationFeedbackDto.StatsResponse getStats(Long orderId) {
        // 존재 여부 검증
        if (!recommendationRepository.existsById(orderId)) {
             throw new BusinessException(ErrorCode.NOT_FOUND, "추천 게시글을 찾을 수 없습니다");
        }

        long likeCount = feedbackRepository.countByRecommendationIdAndReaction(orderId, FeedbackReaction.LIKE);
        long dislikeCount = feedbackRepository.countByRecommendationIdAndReaction(orderId, FeedbackReaction.DISLIKE);

        String myReactionStr = null;
        try {
            Long currentUserId = SecurityUtil.getCurrentUserId().orElse(null);
            if (currentUserId != null) {
                UserEntity user = userRepository.findById(currentUserId).orElse(null);
                if (user != null) {
                    Optional<RecommendationFeedbackEntity> existing = feedbackRepository.findByRecommendationIdAndUserId(orderId, user.getId());
                    if (existing.isPresent()) {
                        myReactionStr = existing.get().getReaction().name();
                    }
                }
            }
        } catch (Exception e) {
            // 토큰 파싱 에러 등으로 내 피드백을 못 가져올 때를 대비 (안전하게 null 반환)
        }

        return RecommendationFeedbackDto.StatsResponse.builder()
                .주문_id(orderId)
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .myFeedback(myReactionStr)
                .build();
    }
}
