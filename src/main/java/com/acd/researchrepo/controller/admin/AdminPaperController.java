package com.acd.researchrepo.controller.admin;

import java.util.List;

import com.acd.researchrepo.dto.external.model.ResearchPaperDto;
import com.acd.researchrepo.dto.external.papers.PaginatedResponse;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.service.ResearchPaperService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

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
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "departmentId", required = false) List<Integer> departmentIds,
            @RequestParam(value = "year", required = false) List<Integer> years,
            @RequestParam(value = "archived", required = false) Boolean archived,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.debug("GET /api/admin/papers endpoint hit");

        PaginatedResponse<ResearchPaperDto> response = researchPaperService.getAdminPapers(
                search,
                departmentIds,
                years,
                archived,
                sortBy,
                sortOrder,
                page,
                size,
                principal);

        return ResponseEntity.ok(response);
    }
}
