package com.acd.researchrepo.controller.admin;

import com.acd.researchrepo.dto.external.model.ResearchPaperDto;
import com.acd.researchrepo.dto.external.papers.PaginatedResponse;
import com.acd.researchrepo.dto.external.papers.ResearchPaperSearchRequest;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.service.ResearchPaperService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/admin/papers")
public class AdminPaperController {

    private final ResearchPaperService researchPaperService;

    public AdminPaperController(ResearchPaperService researchPaperService) {
        this.researchPaperService = researchPaperService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<ResearchPaperDto>> getAdminPapers(
            @Valid ResearchPaperSearchRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.debug("GET /api/admin/papers endpoint hit");

        PaginatedResponse<ResearchPaperDto> response = researchPaperService.getAdminPapers(request, principal);

        return ResponseEntity.ok(response);
    }
}
