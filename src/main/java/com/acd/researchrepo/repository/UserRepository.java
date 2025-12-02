package com.acd.researchrepo.repository;

import com.acd.researchrepo.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
