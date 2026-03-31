package dev.ssafy.domain.recommendationcomment.service;

import dev.ssafy.domain.recommendation.entity.RecommendationEntity;
import dev.ssafy.domain.recommendation.repository.RecommendationRepository;
import dev.ssafy.domain.recommendationcomment.dto.RecommendationCommentDto;
import dev.ssafy.domain.recommendationcomment.entity.RecommendationCommentEntity;
import dev.ssafy.domain.recommendationcomment.entity.RecommendationCommentFeedbackEntity.FeedbackReaction;
import dev.ssafy.domain.recommendationcomment.repository.RecommendationCommentFeedbackRepository;
import dev.ssafy.domain.recommendationcomment.repository.RecommendationCommentRepository;
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
public class RecommendationCommentService {

    private final RecommendationCommentRepository commentRepository;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationCommentFeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public PaginatedResponse<RecommendationCommentDto.Response> getComments(Long orderId, int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<RecommendationCommentEntity> entityPage = commentRepository.findByRecommendationIdWithAuthor(orderId, pageRequest);

        List<Long> commentIds = entityPage.getContent().stream()
                .map(RecommendationCommentEntity::getId)
                .collect(Collectors.toList());

        // 피드백 카운트 가져오기 (N+1 방지)
        Map<Long, Map<FeedbackReaction, Long>> feedbackCounts = new HashMap<>();
        if (!commentIds.isEmpty()) {
            List<Object[]> rawCounts = feedbackRepository.countFeedbackByCommentIds(commentIds);
            for (Object[] row : rawCounts) {
                Long cid = (Long) row[0];
                FeedbackReaction reaction = (FeedbackReaction) row[1];
                Long count = (Long) row[2];
                feedbackCounts.computeIfAbsent(cid, k -> new HashMap<>()).put(reaction, count);
            }
        }

        // 로그인된 사용자의 피드백 확인
        Map<Long, FeedbackReaction> myFeedbackMap = new HashMap<>();
        Optional<Long> currentUserIdOpt = SecurityUtil.getCurrentUserId();
        if (currentUserIdOpt.isPresent() && !commentIds.isEmpty()) {
            UserEntity user = userRepository.findById(currentUserIdOpt.get())
                    .orElse(null);
            if (user != null) {
                List<Object[]> myRawReactions = feedbackRepository.findReactionsByCommentIdsAndUserId(commentIds, user.getId());
                for (Object[] row : myRawReactions) {
                    Long cid = (Long) row[0];
                    FeedbackReaction reaction = (FeedbackReaction) row[1];
                    myFeedbackMap.put(cid, reaction);
                }
            }
        }

        List<RecommendationCommentDto.Response> responseList = entityPage.getContent().stream()
                .map(entity -> {
                    Long cid = entity.getId();
                    Map<FeedbackReaction, Long> counts = feedbackCounts.getOrDefault(cid, new HashMap<>());
                    long likeCount = counts.getOrDefault(FeedbackReaction.LIKE, 0L);
                    long dislikeCount = counts.getOrDefault(FeedbackReaction.DISLIKE, 0L);
                    FeedbackReaction myFeedback = myFeedbackMap.get(cid);

                    return RecommendationCommentDto.Response.of(entity, UserDto.from(entity.getAuthor()), likeCount, dislikeCount, myFeedback);
                })
                .collect(Collectors.toList());

        return new PaginatedResponse<>(responseList, entityPage);
    }

    public RecommendationCommentDto.Response getCommentDetail(Long orderId, Long commentId) {
        RecommendationCommentEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다"));

        if (!entity.getRecommendation().getId().equals(orderId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "이 게시글의 댓글이 아닙니다");
        }

        long likeCount = 0L;
        long dislikeCount = 0L;
        Map<FeedbackReaction, Long> counts = new HashMap<>();
        List<Object[]> rawCounts = feedbackRepository.countFeedbackByCommentIds(List.of(commentId));
        for (Object[] row : rawCounts) {
            FeedbackReaction reaction = (FeedbackReaction) row[1];
            counts.put(reaction, (Long) row[2]);
        }
        likeCount = counts.getOrDefault(FeedbackReaction.LIKE, 0L);
        dislikeCount = counts.getOrDefault(FeedbackReaction.DISLIKE, 0L);

        FeedbackReaction myFeedback = null;
        Optional<Long> currentUserIdOpt = SecurityUtil.getCurrentUserId();
        if (currentUserIdOpt.isPresent()) {
            UserEntity user = userRepository.findById(currentUserIdOpt.get()).orElse(null);
            if (user != null) {
                myFeedback = feedbackRepository.findReactionByCommentIdAndUserId(commentId, user.getId());
            }
        }

        return RecommendationCommentDto.Response.of(entity, UserDto.from(entity.getAuthor()), likeCount, dislikeCount, myFeedback);
    }

    @Transactional
    public RecommendationCommentDto.Response createComment(Long orderId, RecommendationCommentDto.Request request) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        RecommendationEntity recommendation = recommendationRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "추천 게시물을 찾을 수 없습니다"));

        RecommendationCommentEntity entity = RecommendationCommentEntity.builder()
                .recommendation(recommendation)
                .author(user)
                .content(request.get내용())
                .build();

        commentRepository.save(entity);

        return RecommendationCommentDto.Response.of(entity, UserDto.from(user), 0L, 0L, null);
    }

    @Transactional
    public RecommendationCommentDto.Response updateComment(Long orderId, Long commentId, RecommendationCommentDto.Request request) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        RecommendationCommentEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다"));

        if (!entity.getRecommendation().getId().equals(orderId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "이 게시글의 댓글이 아닙니다");
        }

        if (!entity.getAuthor().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "이 댓글을 수정할 권한이 없습니다");
        }

        entity.update(request.get내용());
        
        // Return updated comment
        return getCommentDetail(orderId, commentId);
    }

    @Transactional
    public void deleteComment(Long orderId, Long commentId) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        RecommendationCommentEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다"));

        if (!entity.getRecommendation().getId().equals(orderId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "이 게시글의 댓글이 아닙니다");
        }

        if (!entity.getAuthor().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "이 댓글을 삭제할 권한이 없습니다");
        }

        commentRepository.delete(entity);
    }
}
