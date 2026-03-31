package dev.ssafy.domain.boughtsnack.repository;

import dev.ssafy.domain.boughtsnack.entity.BoughtSnackEntity;
import dev.ssafy.domain.boughtsnack.entity.BoughtSnackStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoughtSnackRepository extends JpaRepository<BoughtSnackEntity, Long> {
    Page<BoughtSnackEntity> findByStatus(BoughtSnackStatusEnum status, Pageable pageable);
}
