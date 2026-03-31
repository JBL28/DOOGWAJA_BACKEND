package dev.ssafy.domain.boughtsnackuserstatus.entity;

import dev.ssafy.domain.boughtsnack.entity.BoughtSnackEntity;
import dev.ssafy.domain.boughtsnack.entity.BoughtSnackStatusEnum;
import dev.ssafy.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "bought_snack_user_status",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"purchase_id", "user_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class BoughtSnackUserStatusEntity {

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
    @Column(name = "status", nullable = false, length = 20)
    private BoughtSnackStatusEnum status;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public BoughtSnackUserStatusEntity(BoughtSnackEntity boughtSnack, UserEntity user, BoughtSnackStatusEnum status) {
        this.boughtSnack = boughtSnack;
        this.user = user;
        this.status = status;
    }

    public void updateStatus(BoughtSnackStatusEnum status) {
        this.status = status;
    }
}
