package com.acd.researchrepo.controller.admin;

import java.util.List;

import com.acd.researchrepo.dto.external.papers.PaginatedResponse;
import com.acd.researchrepo.dto.external.requests.AdminRequestResponse;
import com.acd.researchrepo.dto.external.requests.RejectRequestRequest;
import com.acd.researchrepo.model.RequestStatus;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.service.DocumentRequestService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/admin/requests")
public class AdminRequestController {

    private final DocumentRequestService documentRequestService;

    public AdminRequestController(DocumentRequestService documentRequestService) {
        this.documentRequestService = documentRequestService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<AdminRequestResponse>> getAdminRequests(
            @RequestParam(value = "departmentId", required = false) Integer departmentId,
            @RequestParam(value = "status", required = false) List<RequestStatus> statuses,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "desc") String sortOrder,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.debug("api/admin/requests endpoint hit!!");

        PaginatedResponse<AdminRequestResponse> response = documentRequestService.getAdminRequests(
                departmentId,
                statuses,
                search,
                page,
                size,
                sortBy,
                sortOrder,
                principal);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{requestId}/accept")
    public ResponseEntity<AdminRequestResponse> acceptRequest(
            @PathVariable Integer requestId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.debug("PUT /api/admin/requests/{}/accept endpoint hit", requestId);

        AdminRequestResponse response = documentRequestService.acceptRequest(requestId, principal);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{requestId}/reject")
    public ResponseEntity<AdminRequestResponse> rejectRequest(
            @PathVariable Integer requestId,
            @Valid @RequestBody(required = false) RejectRequestRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.debug("PUT /api/admin/requests/{}/reject endpoint hit", requestId);

        String reason = request != null ? request.getReason() : null;

        AdminRequestResponse response = documentRequestService.rejectRequest(requestId, reason, principal);

        return ResponseEntity.ok(response);
    }
}
