package dev.ssafy.domain.recommendationfeedback.repository;

import dev.ssafy.domain.recommendationfeedback.entity.RecommendationFeedbackEntity;
import dev.ssafy.domain.recommendationfeedback.entity.RecommendationFeedbackEntity.FeedbackReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecommendationFeedbackRepository extends JpaRepository<RecommendationFeedbackEntity, Long> {

    Optional<RecommendationFeedbackEntity> findByRecommendationIdAndUserId(Long orderId, Long userId);

    long countByRecommendationIdAndReaction(Long orderId, FeedbackReaction reaction);

    boolean existsByRecommendationIdAndUserIdAndReaction(Long orderId, Long userId, FeedbackReaction reaction);

    @Query("SELECT r.recommendation.id, r.reaction, COUNT(r) FROM RecommendationFeedbackEntity r WHERE r.recommendation.id IN :orderIds GROUP BY r.recommendation.id, r.reaction")
    List<Object[]> countFeedbackByRecommendationIds(@Param("orderIds") List<Long> orderIds);

    @Query("SELECT r.recommendation.id, r.reaction FROM RecommendationFeedbackEntity r WHERE r.recommendation.id IN :orderIds AND r.user.id = :userId")
    List<Object[]> findReactionsByRecommendationIdsAndUserId(@Param("orderIds") List<Long> orderIds, @Param("userId") Long userId);
}
