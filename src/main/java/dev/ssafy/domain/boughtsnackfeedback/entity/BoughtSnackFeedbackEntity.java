package dev.ssafy.domain.boughtsnackfeedback.entity;

import dev.ssafy.domain.boughtsnack.entity.BoughtSnackEntity;
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
    name = "bs_feedback",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"purchase_id", "user_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class BoughtSnackFeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    private BoughtSnackEntity boughtSnack;

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
    public BoughtSnackFeedbackEntity(BoughtSnackEntity boughtSnack, UserEntity user, FeedbackReaction reaction) {
        this.boughtSnack = boughtSnack;
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
