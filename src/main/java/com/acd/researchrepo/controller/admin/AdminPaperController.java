package com.acd.researchrepo.controller.admin;

import java.io.IOException;

import com.acd.researchrepo.dto.external.model.ResearchPaperDto;
import com.acd.researchrepo.dto.external.papers.PaginatedResponse;
import com.acd.researchrepo.dto.external.papers.PaperCreateRequest;
import com.acd.researchrepo.dto.external.papers.PaperUpdateRequest;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.service.ResearchPaperService;
import com.acd.researchrepo.util.RequestParamValidator;
import com.acd.researchrepo.util.RoleBasedAccess;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "departmentId", required = false) String departmentIdStr,
            @RequestParam(value = "archived", required = false) Boolean archived,
            @RequestParam(value = "year", required = false) String years,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.debug("api/admin/papers endpoint hit");

        validateAdminAccess(principal);
        Integer departmentId = parseDepartmentId(departmentIdStr, principal);
        RequestParamValidator.validatePagination(page, size);
        RequestParamValidator.validateSortParams(
                sortBy,
                sortOrder,
                "title",
                "authorName",
                "submissionDate",
                "createdAt",
                "updatedAt");

        Integer userDepartmentId = getUserDepartmentIdIfDepartmentAdmin(principal);
        Integer effectiveDepartmentId = (userDepartmentId != null) ? userDepartmentId : departmentId;

        // Call service method for admin papers (includes archived)
        PaginatedResponse<ResearchPaperDto> response = researchPaperService.getAdminPapers(
                search,
                effectiveDepartmentId != null ? effectiveDepartmentId.toString() : null,
                archived,
                years,
                sortBy,
                sortOrder,
                page,
                size,
                principal);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResearchPaperDto> getAdminPaperById(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.debug("api/admin/papers/{} endpoint hit", id);

        validateAdminAccess(principal);
        validatePaperId(id);

        ResearchPaperDto paperDto = researchPaperService.getAdminPaperById(id, principal);
        return ResponseEntity.ok(paperDto);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ResearchPaperDto> createPaper(
            @RequestParam("metadata") String metadataJson,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserPrincipal principal) throws IOException {

        log.debug("api/admin/papers POST endpoint hit");

        validateAdminAccess(principal);
        validateFileUpload(file);

        PaperCreateRequest createRequest = researchPaperService.parsePaperCreateRequest(metadataJson);
        ResearchPaperDto createdPaper = researchPaperService.createPaper(createRequest, file, principal);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdPaper);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResearchPaperDto> updatePaper(
            @PathVariable Integer id,
            @Valid @RequestBody PaperUpdateRequest updateRequest,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.debug("api/admin/papers/{} endpoint hit", id);

        validateAdminAccess(principal);
        validatePaperId(id);

        ResearchPaperDto updatedPaper = researchPaperService.updatePaper(id, updateRequest, principal);
        return ResponseEntity.ok(updatedPaper);
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<ResearchPaperDto> archivePaper(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.debug("api/admin/papers/{}/archive endpoint hit", id);

        validateAdminAccess(principal);
        validatePaperId(id);

        ResearchPaperDto paperDto = researchPaperService.archivePaper(id, principal);
        return ResponseEntity.ok(paperDto);
    }

    @PutMapping("/{id}/unarchive")
    public ResponseEntity<ResearchPaperDto> unarchivePaper(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.debug("api/admin/papers/{}/unarchive endpoint hit", id);

        validateAdminAccess(principal);
        validatePaperId(id);

        ResearchPaperDto paperDto = researchPaperService.unarchivePaper(id, principal);
        return ResponseEntity.ok(paperDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaper(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        log.debug("api/admin/papers/{} endpoint hit", id);

        validateAdminAccess(principal);
        validatePaperId(id);

        researchPaperService.deletePaper(id, principal);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private void validateAdminAccess(CustomUserPrincipal principal) {
        if (!RoleBasedAccess.isUserAdmin(principal)) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED, "Authentication required");
        }
    }

    private void validatePaperId(Integer id) {
        if (id == null || id <= 0) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Invalid paper ID");
        }
    }

    private void validateFileUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "File is required");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new ApiException(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "File content type is required");
        }

        if (!contentType.equals("application/pdf") &&
                !contentType.equals("application/msword") &&
                !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            throw new ApiException(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "Only PDF and DOCX files are allowed.");
        }

        if (file.getSize() > 20 * 1024 * 1024) { // 20MB
            throw new ApiException(ErrorCode.FILE_TOO_LARGE, "File exceeds 20MB limit");
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
