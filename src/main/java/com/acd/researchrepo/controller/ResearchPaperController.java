package com.acd.researchrepo.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.acd.researchrepo.dto.external.papers.PaginatedResponseDto;
import com.acd.researchrepo.dto.external.papers.ResearchPaperDto;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.service.ResearchPaperService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/papers")
public class ResearchPaperController {
    private final ResearchPaperService researchPaperService;

    public ResearchPaperController(ResearchPaperService service) {
        this.researchPaperService = service;
    }

    @GetMapping
    public PaginatedResponseDto<ResearchPaperDto> listPapers(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "departmentId", required = false) String departmentIdStr,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "archived", required = false) Boolean archived,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {

        List<Integer> departmentIds;

        if (departmentIdStr == null || departmentIdStr.isEmpty()) {
            departmentIds = null;
        } else {
            departmentIds = Arrays.stream(departmentIdStr.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }

        return researchPaperService.getPapers(
                search,
                departmentIds,
                year,
                archived,
                sortBy,
                sortOrder,
                page,
                size,
                userPrincipal);
    }
}
