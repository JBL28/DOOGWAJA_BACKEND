package dev.ssafy.domain.recommendation.entity;

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
@Table(name = "recommendation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RecommendationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    // rc_id의 생성 전략은 임의로 명시하지 않으나 order_id와 동일한 값을 쓰는 것을 기본으로 함 (REQUIREMENT.md 참고)
    @Column(name = "rc_id", nullable = false)
    private Long rcId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity author;

    @Column(name = "snack_name", nullable = false, length = 100)
    private String snackName;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public RecommendationEntity(UserEntity author, String snackName, String reason) {
        this.author = author;
        this.snackName = snackName;
        this.reason = reason;
        this.rcId = 0L; // 임시값 (NOT NULL 제약조건 통과용, flush 후 ID로 업데이트됨)
    }

    // 영속화 이후에 rc_id를 채우기 위한 헬퍼 (id 발급 후 호출)
    public void syncRcIdWithOrderId() {
        if (this.rcId == null || this.rcId == 0L) {
            this.rcId = this.id;
        }
    }

    public void update(String snackName, String reason) {
        if (snackName != null) this.snackName = snackName;
        if (reason != null) this.reason = reason;
    }
}
