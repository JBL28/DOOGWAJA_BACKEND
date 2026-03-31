package dev.ssafy.domain.user.repository;

import dev.ssafy.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByNickname(String nickname);
    boolean existsByNickname(String nickname);
    Optional<UserEntity> findByRefreshToken(String refreshToken);
}
