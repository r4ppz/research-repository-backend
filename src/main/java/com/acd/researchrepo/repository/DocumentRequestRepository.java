package com.acd.researchrepo.repository;

import java.util.List;
import java.util.Optional;

import com.acd.researchrepo.model.DocumentRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRequestRepository
        extends JpaRepository<DocumentRequest, Integer>, JpaSpecificationExecutor<DocumentRequest> {

    @Query("SELECT dr FROM DocumentRequest dr JOIN dr.paper p WHERE dr.user.userId = :userId AND p.archived = false")
    List<DocumentRequest> findByUserIdAndPaperNotArchived(@Param("userId") Integer userId);

    @Query("SELECT dr FROM DocumentRequest dr JOIN dr.paper p WHERE dr.user.userId = :userId AND dr.paper.paperId = :paperId AND (dr.status = 'PENDING' OR dr.status = 'ACCEPTED')")
    List<DocumentRequest> findByUserIdAndPaperIdAndActiveStatus(
            @Param("userId") Integer userId,
            @Param("paperId") Integer paperId);

    @Query("SELECT dr FROM DocumentRequest dr WHERE dr.requestId = :requestId AND dr.user.userId = :userId")
    Optional<DocumentRequest> findByIdAndUserId(
            @Param("requestId") Integer requestId,
            @Param("userId") Integer userId);

    @Query("SELECT dr FROM DocumentRequest dr WHERE dr.user.userId = :userId AND dr.paper.paperId = :paperId")
    Optional<DocumentRequest> findByUserIdAndPaperId(
            @Param("userId") Integer userId,
            @Param("paperId") Integer paperId);
}
