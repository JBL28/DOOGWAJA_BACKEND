package dev.ssafy.domain.boughtsnackfeedback.repository;

import dev.ssafy.domain.boughtsnackfeedback.entity.BoughtSnackFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoughtSnackFeedbackRepository extends JpaRepository<BoughtSnackFeedbackEntity, Long> {

    @Query("SELECT f.boughtSnack.id, f.reaction, COUNT(f) FROM BoughtSnackFeedbackEntity f WHERE f.boughtSnack.id IN :purchaseIds GROUP BY f.boughtSnack.id, f.reaction")
    List<Object[]> countFeedbackByPurchaseIds(@Param("purchaseIds") List<Long> purchaseIds);

    @Query("SELECT f.boughtSnack.id, f.reaction FROM BoughtSnackFeedbackEntity f WHERE f.boughtSnack.id IN :purchaseIds AND f.user.id = :userId")
    List<Object[]> findReactionsByPurchaseIdsAndUserId(@Param("purchaseIds") List<Long> purchaseIds, @Param("userId") Long userId);

    Optional<BoughtSnackFeedbackEntity> findByBoughtSnackIdAndUserId(Long purchaseId, Long userId);
}
