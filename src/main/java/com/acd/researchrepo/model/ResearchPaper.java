package com.acd.researchrepo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "research_papers", indexes = {
        @Index(name = "idx_papers_department", columnList = "department_id"),
        @Index(name = "idx_papers_submission_date", columnList = "submission_date"),
        @Index(name = "idx_papers_archived", columnList = "archived")
})
@Data
@EntityListeners(AuditingEntityListener.class)
public class ResearchPaper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paperId;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    @NotNull
    private String title;

    @Column(name = "author_name", nullable = false, length = 255)
    @NotNull
    @Size(max = 255)
    private String authorName;

    @Column(name = "abstract_text", nullable = false, columnDefinition = "TEXT")
    @NotNull
    private String abstractText;

    @Column(name = "file_path", nullable = false, length = 512)
    @NotNull
    @Size(max = 512)
    private String filePath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    @NotNull
    private Department department;

    @Column(name = "submission_date", nullable = false)
    @NotNull
    private LocalDate submissionDate;

    @Column(name = "archived", nullable = false)
    @NotNull
    private Boolean archived = false;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @OneToMany(mappedBy = "paper", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentRequest> documentRequests;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
