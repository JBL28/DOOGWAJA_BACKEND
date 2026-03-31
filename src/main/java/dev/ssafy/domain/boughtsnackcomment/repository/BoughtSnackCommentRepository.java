package dev.ssafy.domain.boughtsnackcomment.repository;

import dev.ssafy.domain.boughtsnackcomment.entity.BoughtSnackCommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoughtSnackCommentRepository extends JpaRepository<BoughtSnackCommentEntity, Long> {

    @Query(value = "SELECT c FROM BoughtSnackCommentEntity c JOIN FETCH c.author WHERE c.boughtSnack.id = :purchaseId",
           countQuery = "SELECT count(c) FROM BoughtSnackCommentEntity c WHERE c.boughtSnack.id = :purchaseId")
    Page<BoughtSnackCommentEntity> findByBoughtSnackIdWithAuthor(@Param("purchaseId") Long purchaseId, Pageable pageable);

    long countByBoughtSnackId(Long purchaseId);

    @Query("SELECT c.boughtSnack.id, COUNT(c) FROM BoughtSnackCommentEntity c WHERE c.boughtSnack.id IN :purchaseIds GROUP BY c.boughtSnack.id")
    List<Object[]> countByBoughtSnackIds(@Param("purchaseIds") List<Long> purchaseIds);
}
