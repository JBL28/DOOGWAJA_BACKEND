package dev.ssafy.domain.recommendation.repository;

import dev.ssafy.domain.recommendation.entity.RecommendationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecommendationRepository extends JpaRepository<RecommendationEntity, Long> {

    @Query(value = "SELECT r FROM RecommendationEntity r JOIN FETCH r.author",
           countQuery = "SELECT count(r) FROM RecommendationEntity r")
    Page<RecommendationEntity> findAllWithAuthor(Pageable pageable);

    @Query("SELECT r FROM RecommendationEntity r JOIN FETCH r.author WHERE r.id = :id")
    Optional<RecommendationEntity> findByIdWithAuthor(Long id);
}
