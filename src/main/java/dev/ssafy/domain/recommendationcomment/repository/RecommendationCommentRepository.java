package dev.ssafy.domain.recommendationcomment.repository;

import dev.ssafy.domain.recommendationcomment.entity.RecommendationCommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationCommentRepository extends JpaRepository<RecommendationCommentEntity, Long> {

    @Query(value = "SELECT c FROM RecommendationCommentEntity c JOIN FETCH c.author WHERE c.recommendation.id = :orderId",
           countQuery = "SELECT count(c) FROM RecommendationCommentEntity c WHERE c.recommendation.id = :orderId")
    Page<RecommendationCommentEntity> findByRecommendationIdWithAuthor(@Param("orderId") Long orderId, Pageable pageable);

    long countByRecommendationId(Long orderId);

    @Query("SELECT c.recommendation.id, COUNT(c) FROM RecommendationCommentEntity c WHERE c.recommendation.id IN :orderIds GROUP BY c.recommendation.id")
    List<Object[]> countByRecommendationIds(@Param("orderIds") List<Long> orderIds);
}
