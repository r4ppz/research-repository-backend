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

    /**
     * Deletes expired refresh tokens for a specific user.
     *
     * @param userId the ID of the user whose expired tokens are to be deleted
     * @param now    the current timestamp used to determine expiration
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user.userId = :userId AND r.expiresAt < :now")
    void deleteExpiredByUserId(@Param("userId") Integer userId, @Param("now") LocalDateTime now);
}
