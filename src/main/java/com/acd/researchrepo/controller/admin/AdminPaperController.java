package com.acd.researchrepo.controller.admin;

import com.acd.researchrepo.dto.external.model.ResearchPaperDto;
import com.acd.researchrepo.dto.external.papers.PaginatedResponse;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.service.ResearchPaperService;
import com.acd.researchrepo.util.RoleBasedAccess;

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
            @RequestParam(value = "departmentId", required = false) String departmentIdStr,
            @RequestParam(value = "year", required = false) String years,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.debug("api/admin/papers endpoint hit");

        validateAdminAccess(principal);
        Integer departmentId = parseDepartmentId(departmentIdStr, principal);
        validatePagination(page, size);
        validateSortParams(sortBy, sortOrder);

        Integer userDepartmentId = getUserDepartmentIdIfDepartmentAdmin(principal);
        Integer effectiveDepartmentId = (userDepartmentId != null) ? userDepartmentId : departmentId;

        // Call service method for admin papers (includes archived)
        PaginatedResponse<ResearchPaperDto> response = researchPaperService.getAdminPapers(
                search,
                effectiveDepartmentId != null ? effectiveDepartmentId.toString() : null,
                years,
                sortBy,
                sortOrder,
                page,
                size,
                principal);

        return ResponseEntity.ok(response);
    }

    private void validateAdminAccess(CustomUserPrincipal principal) {
        if (!RoleBasedAccess.isUserAdmin(principal)) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED, "Authentication required");
        }
    }

    private Integer parseDepartmentId(String departmentIdStr, CustomUserPrincipal principal) {
        if (departmentIdStr == null || departmentIdStr.trim().isEmpty()) {
            return null;
        }
        if (RoleBasedAccess.isUserDepartmentAdmin(principal)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "departmentId filter not permitted for your role");
        }
        try {
            int departmentId = Integer.parseInt(departmentIdStr);
            if (departmentId <= 0) {
                throw new ApiException(ErrorCode.INVALID_REQUEST,
                        "Invalid query parameter: departmentId must be a positive integer");
            }
            return departmentId;
        } catch (NumberFormatException e) {
            throw new ApiException(ErrorCode.INVALID_REQUEST,
                    "Invalid query parameter: departmentId must be a valid integer");
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Invalid query parameter: page must be non-negative");
        }
        if (size <= 0 || size > 100) {
            throw new ApiException(ErrorCode.INVALID_REQUEST,
                    "Invalid query parameter: size must be between 1 and 100");
        }
    }

    private void validateSortParams(String sortBy, String sortOrder) {
        if (sortBy != null && !isValidSortByField(sortBy)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Invalid query parameter: sortBy field not allowed");
        }
        if (!"asc".equalsIgnoreCase(sortOrder) && !"desc".equalsIgnoreCase(sortOrder)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST,
                    "Invalid query parameter: sortOrder must be 'asc' or 'desc'");
        }
    }

    private Integer getUserDepartmentIdIfDepartmentAdmin(CustomUserPrincipal principal) {
        if (RoleBasedAccess.isUserDepartmentAdmin(principal)) {
            Integer userDepartmentId = principal.getDepartmentId();
            if (userDepartmentId == null) {
                throw new ApiException(ErrorCode.ACCESS_DENIED, "Department admin not assigned to a department");
            }
            return userDepartmentId;
        }
        return null;
    }

    private boolean isValidSortByField(String field) {
        if (field == null)
            return true;
        return java.util.List.of("title", "authorName", "submissionDate", "createdAt", "updatedAt").contains(field);
    }
}
