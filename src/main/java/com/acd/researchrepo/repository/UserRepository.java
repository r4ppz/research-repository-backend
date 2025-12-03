package com.acd.researchrepo.repository;

import java.util.Optional;

import com.acd.researchrepo.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
}
