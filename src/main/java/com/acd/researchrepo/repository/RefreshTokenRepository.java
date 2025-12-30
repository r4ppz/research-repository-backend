package com.acd.researchrepo.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import com.acd.researchrepo.model.RefreshToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user.userId = :userId")
    void deleteByUserId(@Param("userId") Integer userId);

    // UNUSED
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user.userId = :userId AND r.expiresAt < :now")
    void deleteExpiredByUserId(@Param("userId") Integer userId, @Param("now") LocalDateTime now);
}
