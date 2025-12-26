package com.acd.researchrepo.controller;

import java.util.List;

import com.acd.researchrepo.dto.external.auth.DepartmentDto;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.service.DepartmentService;
import com.acd.researchrepo.service.ResearchPaperService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/filters")
public class FilterController {

    private final ResearchPaperService paperService;
    private final DepartmentService departmentService;

    public FilterController(ResearchPaperService paperService, DepartmentService departmentService) {
        this.paperService = paperService;
        this.departmentService = departmentService;
    }

    @GetMapping("/years")
    public List<Integer> getAvailableYears(@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        return paperService.getAvailableYears(userPrincipal);
    }

    @GetMapping("/departments")
    public List<DepartmentDto> getDepartments(@AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        return departmentService.getAvailableDepartments(userPrincipal);
    }
}
