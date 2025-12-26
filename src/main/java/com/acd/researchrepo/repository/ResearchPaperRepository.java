package com.acd.researchrepo.repository;

import com.acd.researchrepo.model.ResearchPaper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ResearchPaperRepository
        extends JpaRepository<ResearchPaper, Integer>, JpaSpecificationExecutor<ResearchPaper> {
}
