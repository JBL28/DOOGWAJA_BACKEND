package dev.ssafy.domain.recommendationfeedback.entity;

import dev.ssafy.domain.recommendation.entity.RecommendationEntity;
import dev.ssafy.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "rc_feedback",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"order_id", "user_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RecommendationFeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 테이블 정의서 ERD: rcc_feedback에는 댓글_id가 포함되어 있으나,
    // 현재 프론트엔드 API 명세(/recommendations/{주문_id}/like)상 추천 게시글 자체에 대한 피드백으로 취급함.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private RecommendationEntity recommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackReaction reaction;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public RecommendationFeedbackEntity(RecommendationEntity recommendation, UserEntity user, FeedbackReaction reaction) {
        this.recommendation = recommendation;
        this.user = user;
        this.reaction = reaction;
    }

    public void updateReaction(FeedbackReaction reaction) {
        this.reaction = reaction;
    }

    public enum FeedbackReaction {
        LIKE, DISLIKE
    }
}
