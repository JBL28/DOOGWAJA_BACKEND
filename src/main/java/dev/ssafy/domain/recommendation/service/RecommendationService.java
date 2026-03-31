package dev.ssafy.domain.recommendation.service;

import dev.ssafy.domain.recommendation.dto.RecommendationDto;
import dev.ssafy.domain.recommendation.entity.RecommendationEntity;
import dev.ssafy.domain.recommendation.repository.RecommendationRepository;
import dev.ssafy.domain.recommendationcomment.repository.RecommendationCommentRepository;
import dev.ssafy.domain.recommendationfeedback.entity.RecommendationFeedbackEntity.FeedbackReaction;
import dev.ssafy.domain.recommendationfeedback.repository.RecommendationFeedbackRepository;
import dev.ssafy.domain.user.dto.UserDto;
import dev.ssafy.domain.user.entity.UserEntity;
import dev.ssafy.domain.user.repository.UserRepository;
import dev.ssafy.global.exception.BusinessException;
import dev.ssafy.global.exception.ErrorCode;
import dev.ssafy.global.response.PaginatedResponse;
import dev.ssafy.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final RecommendationCommentRepository recommendationCommentRepository;
    private final RecommendationFeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public PaginatedResponse<RecommendationDto.Response> getRecommendations(int page, int pageSize) {
        // 프론트는 1-index, Spring Data JPA는 0-index 기반
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RecommendationEntity> entityPage = recommendationRepository.findAllWithAuthor(pageRequest);

        List<Long> orderIds = entityPage.getContent().stream()
                .map(RecommendationEntity::getId)
                .collect(Collectors.toList());

        // 댓글 수 집계 (N+1 방지)
        Map<Long, Long> commentCountMap = recommendationCommentRepository.countByRecommendationIds(orderIds).stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));

        Map<Long, Map<FeedbackReaction, Long>> feedbackCounts = new HashMap<>();
        if (!orderIds.isEmpty()) {
            feedbackRepository.countFeedbackByRecommendationIds(orderIds).forEach(row -> {
                Long pid = (Long) row[0];
                FeedbackReaction reaction = (FeedbackReaction) row[1];
                Long count = (Long) row[2];
                feedbackCounts.computeIfAbsent(pid, k -> new HashMap<>()).put(reaction, count);
            });
        }

        Map<Long, FeedbackReaction> myFeedbackMap = new HashMap<>();
        Optional<Long> currentUserIdOpt = SecurityUtil.getCurrentUserId();
        if (currentUserIdOpt.isPresent() && !orderIds.isEmpty()) {
            UserEntity user = userRepository.findById(currentUserIdOpt.get()).orElse(null);
            if (user != null) {
                feedbackRepository.findReactionsByRecommendationIdsAndUserId(orderIds, user.getId()).forEach(row -> {
                    myFeedbackMap.put((Long) row[0], (FeedbackReaction) row[1]);
                });
            }
        }

        List<RecommendationDto.Response> responseList = entityPage.getContent().stream()
                .map(entity -> {
                    long commentCount = commentCountMap.getOrDefault(entity.getId(), 0L);
                    
                    Map<FeedbackReaction, Long> counts = feedbackCounts.getOrDefault(entity.getId(), new HashMap<>());
                    long likeCount = counts.getOrDefault(FeedbackReaction.LIKE, 0L);
                    long dislikeCount = counts.getOrDefault(FeedbackReaction.DISLIKE, 0L);
                    FeedbackReaction myFeedback = myFeedbackMap.get(entity.getId());
                    
                    return RecommendationDto.Response.of(entity, UserDto.from(entity.getAuthor()), commentCount, likeCount, dislikeCount, myFeedback);
                })
                .collect(Collectors.toList());

        return new PaginatedResponse<>(responseList, entityPage);
    }

    public RecommendationDto.Response getRecommendationDetail(Long orderId) {
        RecommendationEntity entity = recommendationRepository.findByIdWithAuthor(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "추천 게시물을 찾을 수 없습니다"));

        long commentCount = recommendationCommentRepository.countByRecommendationId(orderId);

        long likeCount = feedbackRepository.countByRecommendationIdAndReaction(orderId, FeedbackReaction.LIKE);
        long dislikeCount = feedbackRepository.countByRecommendationIdAndReaction(orderId, FeedbackReaction.DISLIKE);
        
        FeedbackReaction myFeedback = null;
        Optional<Long> currentUserIdOpt = SecurityUtil.getCurrentUserId();
        if (currentUserIdOpt.isPresent()) {
            UserEntity user = userRepository.findById(currentUserIdOpt.get()).orElse(null);
            if (user != null) {
                myFeedback = feedbackRepository.findByRecommendationIdAndUserId(orderId, user.getId())
                        .map(f -> f.getReaction()).orElse(null);
            }
        }

        return RecommendationDto.Response.of(entity, UserDto.from(entity.getAuthor()), commentCount, likeCount, dislikeCount, myFeedback);
    }

    @Transactional
    public RecommendationDto.Response createRecommendation(RecommendationDto.Request request) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        RecommendationEntity entity = RecommendationEntity.builder()
                .author(user)
                .snackName(request.get과자이름())
                .reason(request.get주문이유())
                .build();

        recommendationRepository.save(entity);
        entity.syncRcIdWithOrderId(); // rc_id는 order_id와 동일하게

        return RecommendationDto.Response.of(entity, UserDto.from(user), 0L, 0L, 0L, null);
    }

    @Transactional
    public RecommendationDto.Response updateRecommendation(Long orderId, RecommendationDto.UpdateRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        RecommendationEntity entity = recommendationRepository.findByIdWithAuthor(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "추천 게시물을 찾을 수 없습니다"));

        if (!entity.getAuthor().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "이 게시물을 수정할 권한이 없습니다");
        }

        entity.update(request.get과자이름(), request.get주문이유());

        long commentCount = recommendationCommentRepository.countByRecommendationId(orderId);
        long likeCount = feedbackRepository.countByRecommendationIdAndReaction(orderId, FeedbackReaction.LIKE);
        long dislikeCount = feedbackRepository.countByRecommendationIdAndReaction(orderId, FeedbackReaction.DISLIKE);
        
        FeedbackReaction myFeedback = null;
        Optional<Long> currentUserIdOpt = SecurityUtil.getCurrentUserId();
        if (currentUserIdOpt.isPresent()) {
            UserEntity user = userRepository.findById(currentUserIdOpt.get()).orElse(null);
            if (user != null) {
                myFeedback = feedbackRepository.findByRecommendationIdAndUserId(orderId, user.getId())
                        .map(f -> f.getReaction()).orElse(null);
            }
        }

        return RecommendationDto.Response.of(entity, UserDto.from(entity.getAuthor()), commentCount, likeCount, dislikeCount, myFeedback);
    }

    @Transactional
    public void deleteRecommendation(Long orderId) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        RecommendationEntity entity = recommendationRepository.findByIdWithAuthor(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "추천 게시물을 찾을 수 없습니다"));

        if (!entity.getAuthor().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "이 게시물을 삭제할 권한이 없습니다");
        }

        recommendationRepository.delete(entity);
    }
}
