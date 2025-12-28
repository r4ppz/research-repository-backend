package com.acd.researchrepo.repository;

import java.util.List;

import com.acd.researchrepo.model.DocumentRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRequestRepository extends JpaRepository<DocumentRequest, Integer> {

    @Query("SELECT dr FROM DocumentRequest dr JOIN dr.paper p WHERE dr.user.userId = :userId AND p.archived = false")
    List<DocumentRequest> findByUserIdAndPaperNotArchived(@Param("userId") Integer userId);
}
