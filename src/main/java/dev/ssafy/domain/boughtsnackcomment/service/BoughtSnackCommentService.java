package dev.ssafy.domain.boughtsnackcomment.service;

import dev.ssafy.domain.boughtsnack.entity.BoughtSnackEntity;
import dev.ssafy.domain.boughtsnack.repository.BoughtSnackRepository;
import dev.ssafy.domain.boughtsnackcomment.dto.BoughtSnackCommentDto;
import dev.ssafy.domain.boughtsnackcomment.entity.BoughtSnackCommentEntity;
import dev.ssafy.domain.boughtsnackcomment.repository.BoughtSnackCommentRepository;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoughtSnackCommentService {

    private final BoughtSnackCommentRepository commentRepository;
    private final BoughtSnackRepository boughtSnackRepository;
    private final UserRepository userRepository;

    public PaginatedResponse<BoughtSnackCommentDto.Response> getComments(Long purchaseId, int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<BoughtSnackCommentEntity> entityPage = commentRepository.findByBoughtSnackIdWithAuthor(purchaseId, pageRequest);

        List<BoughtSnackCommentDto.Response> responseList = entityPage.getContent().stream()
                .map(entity -> BoughtSnackCommentDto.Response.of(entity, UserDto.from(entity.getAuthor()), 0L, 0L, null))
                .collect(Collectors.toList());

        return new PaginatedResponse<>(responseList, entityPage);
    }

    public BoughtSnackCommentDto.Response getCommentDetail(Long purchaseId, Long commentId) {
        BoughtSnackCommentEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다"));

        if (!entity.getBoughtSnack().getId().equals(purchaseId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "이 과자의 댓글이 아닙니다");
        }

        return BoughtSnackCommentDto.Response.of(entity, UserDto.from(entity.getAuthor()), 0L, 0L, null);
    }

    @Transactional
    public BoughtSnackCommentDto.Response createComment(Long purchaseId, BoughtSnackCommentDto.Request request) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        BoughtSnackEntity boughtSnack = boughtSnackRepository.findById(purchaseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "구매 과자를 찾을 수 없습니다"));

        BoughtSnackCommentEntity entity = BoughtSnackCommentEntity.builder()
                .boughtSnack(boughtSnack)
                .author(user)
                .content(request.get내용())
                .build();

        commentRepository.save(entity);

        return BoughtSnackCommentDto.Response.of(entity, UserDto.from(user), 0L, 0L, null);
    }

    @Transactional
    public BoughtSnackCommentDto.Response updateComment(Long purchaseId, Long commentId, BoughtSnackCommentDto.Request request) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        BoughtSnackCommentEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다"));

        if (!entity.getBoughtSnack().getId().equals(purchaseId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "이 과자의 댓글이 아닙니다");
        }

        if (!entity.getAuthor().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "이 댓글을 수정할 권한이 없습니다");
        }

        entity.update(request.get내용());
        
        return getCommentDetail(purchaseId, commentId);
    }

    @Transactional
    public void deleteComment(Long purchaseId, Long commentId) {
        Long currentUserId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));

        BoughtSnackCommentEntity entity = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다"));

        if (!entity.getBoughtSnack().getId().equals(purchaseId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "이 과자의 댓글이 아닙니다");
        }

        if (!entity.getAuthor().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "이 댓글을 삭제할 권한이 없습니다");
        }

        commentRepository.delete(entity);
    }
}
