package com.acd.researchrepo.repository;

import com.acd.researchrepo.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
  Optional<User> findByEmail(String email);

  Optional<User> findById(Integer userId);
}
