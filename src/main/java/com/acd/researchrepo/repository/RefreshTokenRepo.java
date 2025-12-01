package com.acd.researchrepo.repository;

import com.acd.researchrepo.model.RefreshToken;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {

}
