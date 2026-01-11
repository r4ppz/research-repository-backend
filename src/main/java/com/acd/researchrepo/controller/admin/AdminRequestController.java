package com.acd.researchrepo.controller.admin;

import java.util.List;

import com.acd.researchrepo.dto.external.papers.PaginatedResponse;
import com.acd.researchrepo.dto.external.requests.AdminRequestDetailResponse;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.model.RequestStatus;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.service.DocumentRequestService;
import com.acd.researchrepo.util.RequestParamValidator;
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
@RequestMapping("/api/admin/requests")
public class AdminRequestController {

    private final DocumentRequestService documentRequestService;

    public AdminRequestController(DocumentRequestService documentRequestService) {
        this.documentRequestService = documentRequestService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<AdminRequestDetailResponse>> getAdminRequests(
            @RequestParam(value = "departmentId", required = false) String departmentIdStr,
            @RequestParam(value = "status", required = false) String statusStr,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "desc") String sortOrder,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.debug("api/admin/requests endpoint hit!!");

        validateAdminAccess(principal);
        Integer departmentId = parseDepartmentId(departmentIdStr, principal);
        List<RequestStatus> statuses = parseStatuses(statusStr);
        RequestParamValidator.validatePagination(page, size);
        RequestParamValidator.validateSortParams(sortBy, sortOrder, "createdAt", "status", "paper.title",
                "user.fullName");

        Integer userDepartmentId = getUserDepartmentIdIfDepartmentAdmin(principal);

        PaginatedResponse<AdminRequestDetailResponse> response = documentRequestService.getAdminRequests(
                departmentId,
                userDepartmentId,
                statuses,
                search,
                page,
                size,
                sortBy,
                sortOrder,
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

    private List<RequestStatus> parseStatuses(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return null;
        }
        String[] statusArray = statusStr.split(",");
        return java.util.Arrays.stream(statusArray)
                .map(String::trim)
                .map(this::parseStatus)
                .toList();
    }

    private RequestStatus parseStatus(String status) {
        try {
            return RequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.INVALID_REQUEST,
                    "Invalid query parameter: invalid status value '" + status + "'");
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
}
