package dev.ssafy.domain.recommendationcomment.entity;

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
    name = "rcc_feedback",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "comment_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RecommendationCommentFeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private RecommendationCommentEntity comment;

    // order_id is logically redundant but defined in the ERD. 
    @Column(name = "order_id", nullable = false)
    private Long orderId;

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
    public RecommendationCommentFeedbackEntity(RecommendationCommentEntity comment, Long orderId, UserEntity user, FeedbackReaction reaction) {
        this.comment = comment;
        this.orderId = orderId;
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
