package com.acd.researchrepo.controller;

import com.acd.researchrepo.dto.external.model.ResearchPaperDto;
import com.acd.researchrepo.dto.external.papers.PaginatedResponse;
import com.acd.researchrepo.dto.external.papers.PaperUserRequestResponse;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.service.ResearchPaperService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/papers")
public class ResearchPaperController {
    private final ResearchPaperService researchPaperService;

    public ResearchPaperController(ResearchPaperService service) {
        this.researchPaperService = service;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<ResearchPaperDto>> listPapers(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "departmentId", required = false) String departmentId,
            @RequestParam(value = "year", required = false) String years,
            @RequestParam(value = "archived", required = false) Boolean archived,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        log.debug("api/papers endpoint hit");

        return ResponseEntity.ok(researchPaperService.getPapers(
                search,
                departmentId,
                years,
                archived,
                sortBy,
                sortOrder,
                page,
                size,
                userPrincipal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResearchPaperDto> getPaperById(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        log.debug("api/papers/{} endpoint hit", id);

        if (id == null || id <= 0) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Invalid paper ID");
        }

        return ResponseEntity.ok(researchPaperService.getPaperById(id, userPrincipal));
    }

    @GetMapping("/{id}/my-request")
    public ResponseEntity<PaperUserRequestResponse> getUserRequestForPaper(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        log.debug("api/papers/{}/my-request endpoint hit", id);

        if (id == null || id <= 0) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Invalid paper ID");
        }

        PaperUserRequestResponse response = researchPaperService.getUserRequestForPaper(id, userPrincipal);
        return ResponseEntity.ok(response);
    }
}
