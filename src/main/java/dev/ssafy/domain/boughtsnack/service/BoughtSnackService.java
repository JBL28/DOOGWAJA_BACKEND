package dev.ssafy.domain.boughtsnack.service;

import dev.ssafy.domain.boughtsnack.dto.BoughtSnackDto;
import dev.ssafy.domain.boughtsnack.entity.BoughtSnackEntity;
import dev.ssafy.domain.boughtsnack.entity.BoughtSnackStatusEnum;
import dev.ssafy.domain.boughtsnack.repository.BoughtSnackRepository;
import dev.ssafy.domain.boughtsnackcomment.repository.BoughtSnackCommentRepository;
import dev.ssafy.domain.boughtsnackfeedback.entity.BoughtSnackFeedbackEntity.FeedbackReaction;
import dev.ssafy.domain.boughtsnackfeedback.repository.BoughtSnackFeedbackRepository;
import dev.ssafy.domain.boughtsnackuserstatus.repository.BoughtSnackUserStatusRepository;
import dev.ssafy.domain.user.entity.UserEntity;
import dev.ssafy.domain.user.entity.UserRole;
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
public class BoughtSnackService {

    private final BoughtSnackRepository boughtSnackRepository;
    private final BoughtSnackCommentRepository commentRepository;
    private final BoughtSnackFeedbackRepository feedbackRepository;
    private final BoughtSnackUserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    public PaginatedResponse<BoughtSnackDto.Response> getBoughtSnacks(int page, int pageSize, BoughtSnackStatusEnum status) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BoughtSnackEntity> entityPage;
        
        if (status != null) {
            entityPage = boughtSnackRepository.findByStatus(status, pageRequest);
        } else {
            entityPage = boughtSnackRepository.findAll(pageRequest);
        }

        List<Long> purchaseIds = entityPage.getContent().stream()
                .map(BoughtSnackEntity::getId)
                .collect(Collectors.toList());

        // 댓글 수 집계
        Map<Long, Long> commentCountMap = new HashMap<>();
        if (!purchaseIds.isEmpty()) {
            commentRepository.countByBoughtSnackIds(purchaseIds).forEach(row -> {
                commentCountMap.put((Long) row[0], (Long) row[1]);
            });
        }

        // 피드백 집계
        Map<Long, Map<FeedbackReaction, Long>> feedbackCounts = new HashMap<>();
        if (!purchaseIds.isEmpty()) {
            feedbackRepository.countFeedbackByPurchaseIds(purchaseIds).forEach(row -> {
                Long pid = (Long) row[0];
                FeedbackReaction reaction = (FeedbackReaction) row[1];
                Long count = (Long) row[2];
                feedbackCounts.computeIfAbsent(pid, k -> new HashMap<>()).put(reaction, count);
            });
        }

        // 로그인된 사용자의 피드백 및 개인 상태
        Map<Long, FeedbackReaction> myFeedbackMap = new HashMap<>();
        Map<Long, BoughtSnackStatusEnum> myStatusMap = new HashMap<>();
        Optional<Long> currentUserIdOpt = SecurityUtil.getCurrentUserId();
        
        if (currentUserIdOpt.isPresent() && !purchaseIds.isEmpty()) {
            UserEntity user = userRepository.findById(currentUserIdOpt.get()).orElse(null);
            if (user != null) {
                feedbackRepository.findReactionsByPurchaseIdsAndUserId(purchaseIds, user.getId()).forEach(row -> {
                    myFeedbackMap.put((Long) row[0], (FeedbackReaction) row[1]);
                });
                
                userStatusRepository.findStatusByPurchaseIdsAndUserId(purchaseIds, user.getId()).forEach(row -> {
                    myStatusMap.put((Long) row[0], (BoughtSnackStatusEnum) row[1]);
                });
            }
        }

        List<BoughtSnackDto.Response> responseList = entityPage.getContent().stream()
                .map(entity -> {
                    Long pid = entity.getId();
                    long commentCount = commentCountMap.getOrDefault(pid, 0L);
                    
                    Map<FeedbackReaction, Long> counts = feedbackCounts.getOrDefault(pid, new HashMap<>());
                    long likeCount = counts.getOrDefault(FeedbackReaction.LIKE, 0L);
                    long dislikeCount = counts.getOrDefault(FeedbackReaction.DISLIKE, 0L);
                    
                    FeedbackReaction myFeedback = myFeedbackMap.get(pid);
                    BoughtSnackStatusEnum myStatus = myStatusMap.get(pid);

                    return BoughtSnackDto.Response.of(entity, commentCount, likeCount, dislikeCount, myFeedback, myStatus);
                })
                .collect(Collectors.toList());

        return new PaginatedResponse<>(responseList, entityPage);
    }

    public BoughtSnackDto.Response getBoughtSnackDetail(Long purchaseId) {
        BoughtSnackEntity entity = boughtSnackRepository.findById(purchaseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "구매 과자를 찾을 수 없습니다"));

        long commentCount = commentRepository.countByBoughtSnackId(purchaseId);

        Map<FeedbackReaction, Long> counts = new HashMap<>();
        feedbackRepository.countFeedbackByPurchaseIds(List.of(purchaseId)).forEach(row -> {
            counts.put((FeedbackReaction) row[1], (Long) row[2]);
        });
        long likeCount = counts.getOrDefault(FeedbackReaction.LIKE, 0L);
        long dislikeCount = counts.getOrDefault(FeedbackReaction.DISLIKE, 0L);

        FeedbackReaction myFeedback = null;
        BoughtSnackStatusEnum myStatus = null;
        
        Optional<Long> currentUserIdOpt = SecurityUtil.getCurrentUserId();
        if (currentUserIdOpt.isPresent()) {
            UserEntity user = userRepository.findById(currentUserIdOpt.get()).orElse(null);
            if (user != null) {
                myFeedback = feedbackRepository.findByBoughtSnackIdAndUserId(purchaseId, user.getId())
                        .map(f -> f.getReaction()).orElse(null);
                myStatus = userStatusRepository.findByBoughtSnackIdAndUserId(purchaseId, user.getId())
                        .map(s -> s.getStatus()).orElse(null);
            }
        }

        return BoughtSnackDto.Response.of(entity, commentCount, likeCount, dislikeCount, myFeedback, myStatus);
    }

    @Transactional
    public BoughtSnackDto.Response createBoughtSnack(BoughtSnackDto.Request request) {
        verifyAdminAccess();

        BoughtSnackEntity entity = BoughtSnackEntity.builder()
                .snackName(request.get과자이름())
                .status(request.get상태()) // null safe handled in entity
                .build();

        boughtSnackRepository.save(entity);

        return BoughtSnackDto.Response.of(entity, 0L, 0L, 0L, null, null);
    }

    @Transactional
    public BoughtSnackDto.Response updateBoughtSnack(Long purchaseId, BoughtSnackDto.UpdateRequest request) {
        verifyAdminAccess();

        BoughtSnackEntity entity = boughtSnackRepository.findById(purchaseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "구매 과자를 찾을 수 없습니다"));

        entity.update(request.get과자이름(), request.get상태());

        return getBoughtSnackDetail(purchaseId);
    }

    @Transactional
    public void deleteBoughtSnack(Long purchaseId) {
        verifyAdminAccess();

        BoughtSnackEntity entity = boughtSnackRepository.findById(purchaseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "구매 과자를 찾을 수 없습니다"));

        boughtSnackRepository.delete(entity);
    }

    private void verifyAdminAccess() {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "관리자만 과자를 관리할 수 있습니다");
        }
    }
}
