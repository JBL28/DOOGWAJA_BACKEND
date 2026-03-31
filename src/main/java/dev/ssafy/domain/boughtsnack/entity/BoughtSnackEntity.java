package dev.ssafy.domain.boughtsnack.entity;

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
@Table(name = "bought_snack")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class BoughtSnackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Long id;

    @Column(name = "snack_name", nullable = false, length = 100)
    private String snackName;

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
    public BoughtSnackEntity(String snackName, BoughtSnackStatusEnum status) {
        this.snackName = snackName;
        this.status = status != null ? status : BoughtSnackStatusEnum.재고있음;
    }

    public void update(String snackName, BoughtSnackStatusEnum status) {
        if (snackName != null) this.snackName = snackName;
        if (status != null) this.status = status;
    }
}
