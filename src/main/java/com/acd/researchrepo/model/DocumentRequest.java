package com.acd.researchrepo.model;

import java.time.LocalDateTime;

import com.acd.researchrepo.model.enums.RequestStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "document_requests", indexes = {
        @Index(name = "idx_requests_user", columnList = "user_id"),
        @Index(name = "idx_requests_paper", columnList = "paper_id")
})
public class DocumentRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    @NotNull
    private ResearchPaper paper;

    @Column(name = "request_date", nullable = false)
    @NotNull
    private LocalDateTime requestDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull
    private RequestStatus status = RequestStatus.PENDING;

    // Note: The partial unique index for preventing duplicate PENDING/ACCEPTED
    // requests
    // isn't directly enforceable in JPA; handle via custom repository logic or DB
    // triggers.
}
