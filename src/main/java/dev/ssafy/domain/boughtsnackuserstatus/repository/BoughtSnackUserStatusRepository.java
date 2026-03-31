package dev.ssafy.domain.boughtsnackuserstatus.repository;

import dev.ssafy.domain.boughtsnackuserstatus.entity.BoughtSnackUserStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoughtSnackUserStatusRepository extends JpaRepository<BoughtSnackUserStatusEntity, Long> {

    @Query("SELECT s.boughtSnack.id, s.status FROM BoughtSnackUserStatusEntity s WHERE s.boughtSnack.id IN :purchaseIds AND s.user.id = :userId")
    List<Object[]> findStatusByPurchaseIdsAndUserId(@Param("purchaseIds") List<Long> purchaseIds, @Param("userId") Long userId);

    Optional<BoughtSnackUserStatusEntity> findByBoughtSnackIdAndUserId(Long purchaseId, Long userId);
}
