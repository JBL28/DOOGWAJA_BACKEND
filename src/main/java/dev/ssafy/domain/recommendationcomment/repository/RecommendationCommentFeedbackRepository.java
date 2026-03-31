package dev.ssafy.domain.recommendationcomment.repository;

import dev.ssafy.domain.recommendationcomment.entity.RecommendationCommentFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationCommentFeedbackRepository extends JpaRepository<RecommendationCommentFeedbackEntity, Long> {

    @Query("SELECT f.comment.id, f.reaction, COUNT(f) FROM RecommendationCommentFeedbackEntity f WHERE f.comment.id IN :commentIds GROUP BY f.comment.id, f.reaction")
    List<Object[]> countFeedbackByCommentIds(@Param("commentIds") List<Long> commentIds);

    @Query("SELECT f.reaction FROM RecommendationCommentFeedbackEntity f WHERE f.comment.id = :commentId AND f.user.id = :userId")
    RecommendationCommentFeedbackEntity.FeedbackReaction findReactionByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Query("SELECT f.comment.id, f.reaction FROM RecommendationCommentFeedbackEntity f WHERE f.comment.id IN :commentIds AND f.user.id = :userId")
    List<Object[]> findReactionsByCommentIdsAndUserId(@Param("commentIds") List<Long> commentIds, @Param("userId") Long userId);
}
