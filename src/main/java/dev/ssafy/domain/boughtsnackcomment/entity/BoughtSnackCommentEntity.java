package dev.ssafy.domain.boughtsnackcomment.entity;

import dev.ssafy.domain.boughtsnack.entity.BoughtSnackEntity;
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
@Table(name = "bs_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class BoughtSnackCommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_key")
    private Long id; // ERD에 Key라 되어 있으나 id로 매핑

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    private BoughtSnackEntity boughtSnack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity author;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public BoughtSnackCommentEntity(BoughtSnackEntity boughtSnack, UserEntity author, String content) {
        this.boughtSnack = boughtSnack;
        this.author = author;
        this.content = content;
    }

    public void update(String content) {
        if (content != null) this.content = content;
    }
}
