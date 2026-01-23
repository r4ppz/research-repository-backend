package com.acd.researchrepo.service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.acd.researchrepo.dto.external.model.ResearchPaperDto;
import com.acd.researchrepo.dto.external.papers.PaginatedResponse;
import com.acd.researchrepo.dto.external.papers.PaperCreateRequest;
import com.acd.researchrepo.dto.external.papers.PaperUserRequestResponse;
import com.acd.researchrepo.dto.external.papers.ResearchPaperSearchRequest;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.mapper.ResearchPaperMapper;
import com.acd.researchrepo.model.DocumentRequest;
import com.acd.researchrepo.model.RequestStatus;
import com.acd.researchrepo.model.ResearchPaper;
import com.acd.researchrepo.repository.DepartmentRepository;
import com.acd.researchrepo.repository.DocumentRequestRepository;
import com.acd.researchrepo.repository.ResearchPaperRepository;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.spec.ResearchPaperSpec;
import com.acd.researchrepo.util.RoleBasedAccess;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ResearchPaperService {
    private final ResearchPaperRepository researchPaperRepository;
    private final DocumentRequestRepository documentRequestRepository;
    private final ResearchPaperMapper researchPaperMapper;
    private final DocumentRequestService documentRequestService;
    private final FileStorageService fileStorageService;
    private final DepartmentRepository departmentRepository;

    public ResearchPaperService(
            ResearchPaperRepository researchPaperRepository,
            DocumentRequestRepository documentRequestRepository,
            ResearchPaperMapper researchPaperMapper,
            DocumentRequestService documentRequestService,
            FileStorageService fileStorageService,
            DepartmentRepository departmentRepository) {
        this.researchPaperRepository = researchPaperRepository;
        this.documentRequestRepository = documentRequestRepository;
        this.researchPaperMapper = researchPaperMapper;
        this.documentRequestService = documentRequestService;
        this.fileStorageService = fileStorageService;
        this.departmentRepository = departmentRepository;
    }

    public PaginatedResponse<ResearchPaperDto> getPapers(
            ResearchPaperSearchRequest request,
            CustomUserPrincipal userPrincipal) {

        Boolean archived = request.getArchived();
        if (RoleBasedAccess.isUserStudent(userPrincipal)) {
            archived = false;
        }

        Specification<ResearchPaper> spec = ResearchPaperSpec.build(
                request.getSearch(),
                request.getDepartmentId(),
                request.getYear(),
                archived);

        Page<ResearchPaper> paperPage = researchPaperRepository.findAll(spec, request.toPageable());

        return PaginatedResponse.fromPage(paperPage, researchPaperMapper::toDto);
    }

    /**
     * Get papers for admin management with department scoping.
     * DEPARTMENT_ADMIN: only sees papers in their department (departmentIds param
     * ignored).
     * SUPER_ADMIN: sees all papers, can filter by departmentIds.
     */
    public PaginatedResponse<ResearchPaperDto> getAdminPapers(
            ResearchPaperSearchRequest request,
            CustomUserPrincipal userPrincipal) {

        // Authorization check: must be admin
        if (!RoleBasedAccess.isUserAdmin(userPrincipal)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Admin privileges required");
        }

        // Determine department filtering based on role
        List<Integer> effectiveDepartmentIds = null;
        if (RoleBasedAccess.isUserDepartmentAdmin(userPrincipal)) {
            // Ignore departmentIds param, always scope to their department
            Integer userDeptId = userPrincipal.getDepartmentId();
            if (userDeptId == null) {
                throw new ApiException(ErrorCode.ACCESS_DENIED, "Department admin not assigned to a department");
            }
            effectiveDepartmentIds = List.of(userDeptId);
        } else {
            // For SuperAdmin use provided departmentIds (can be null for all departments)
            effectiveDepartmentIds = request.getDepartmentId();
        }

        Specification<ResearchPaper> spec = ResearchPaperSpec.buildAdmin(
                request.getSearch(),
                effectiveDepartmentIds,
                request.getYear(),
                request.getArchived());

        Page<ResearchPaper> paperPage = researchPaperRepository.findAll(spec, request.toPageable());

        return PaginatedResponse.fromPage(paperPage, researchPaperMapper::toDto);
    }

    public ResearchPaperDto getPaperById(Integer id, CustomUserPrincipal userPrincipal) {
        Optional<ResearchPaper> paperOpt = researchPaperRepository.findById(id);

        if (paperOpt.isEmpty()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Paper not found");
        }

        ResearchPaper paper = paperOpt.get();

        if (RoleBasedAccess.isUserStudentOrTeacher(userPrincipal) && paper.getArchived()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Paper not found");
        }

        return researchPaperMapper.toDto(paper);
    }

    public List<Integer> getAvailableYears(CustomUserPrincipal user) {
        Integer deptId = RoleBasedAccess.isUserDepartmentAdmin(user) ? user.getDepartmentId() : null;
        boolean onlyActive = RoleBasedAccess.isUserStudent(user);
        return researchPaperRepository.findDistinctYears(deptId, onlyActive);
    }

    public PaperUserRequestResponse getUserRequestForPaper(Integer paperId, CustomUserPrincipal userPrincipal) {
        return documentRequestService.getUserRequestForPaper(paperId, userPrincipal);
    }

    @Transactional
    public void archivePaper(Integer id, CustomUserPrincipal principal) {
        ResearchPaper paper = getAndVerifyAdminAccess(id, principal);

        paper.setArchived(true);
        paper.setArchivedAt(LocalDateTime.now());
        researchPaperRepository.save(paper);

        // side-effects: Reject all active requests
        List<DocumentRequest> activeRequests = documentRequestRepository.findByPaperPaperIdAndStatusIn(
                id, List.of(RequestStatus.PENDING, RequestStatus.ACCEPTED));

        for (DocumentRequest request : activeRequests) {
            request.setStatus(RequestStatus.REJECTED);
            request.setRejectionReason("Paper archived");
            documentRequestRepository.save(request);
        }
    }

    @Transactional
    public ResearchPaperDto updatePaper(
            Integer id,
            PaperCreateRequest metadata,
            CustomUserPrincipal principal) {

        ResearchPaper paper = getAndVerifyAdminAccess(id, principal);

        // Update basic fields
        paper.setTitle(metadata.getTitle());
        paper.setAuthorName(metadata.getAuthorName());
        paper.setAbstractText(metadata.getAbstractText());
        paper.setSubmissionDate(metadata.getSubmissionDate());

        // Update department if changed
        if (!paper.getDepartment().getDepartmentId().equals(metadata.getDepartmentId())) {
            // If DEPARTMENT_ADMIN, they can't change it to another department
            if (RoleBasedAccess.isUserDepartmentAdmin(principal)) {
                throw new ApiException(ErrorCode.ACCESS_DENIED, "You can only manage papers within your department");
            }

            var department = departmentRepository.findById(metadata.getDepartmentId())
                    .orElseThrow(() -> new ApiException(ErrorCode.VALIDATION_ERROR, "Department not found"));
            paper.setDepartment(department);
        }

        ResearchPaper savedPaper = researchPaperRepository.save(paper);
        return researchPaperMapper.toDto(savedPaper);
    }

    @Transactional
    public void deletePaper(Integer id, CustomUserPrincipal principal) {
        ResearchPaper paper = getAndVerifyAdminAccess(id, principal);
        String relativePath = paper.getFilePath();

        // Delete from database
        researchPaperRepository.delete(paper);

        // Delete physical file
        fileStorageService.deleteFile(relativePath);
    }

    @Transactional
    public void unarchivePaper(Integer id, CustomUserPrincipal principal) {
        ResearchPaper paper = getAndVerifyAdminAccess(id, principal);

        paper.setArchived(false);
        paper.setArchivedAt(null);
        researchPaperRepository.save(paper);
    }

    public Path downloadPaper(Integer paperId, CustomUserPrincipal principal) {
        ResearchPaper paper = researchPaperRepository.findById(paperId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Paper not found"));

        validateDownloadAccess(paper, principal);

        return fileStorageService.loadFile(paper.getFilePath());
    }

    @Transactional
    public ResearchPaperDto createPaper(
            PaperCreateRequest metadata,
            MultipartFile file,
            CustomUserPrincipal principal) {

        // Authorization & Role-based validation
        if (!RoleBasedAccess.isUserAdmin(principal)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Admin privileges required");
        }

        if (RoleBasedAccess.isUserDepartmentAdmin(principal)) {
            if (!principal.getDepartmentId().equals(metadata.getDepartmentId())) {
                throw new ApiException(ErrorCode.ACCESS_DENIED, "You can only manage papers within your department");
            }
        }

        // Validate department exists
        var department = departmentRepository.findById(metadata.getDepartmentId())
                .orElseThrow(() -> new ApiException(ErrorCode.VALIDATION_ERROR, "Department not found"));

        // Create Entity (temporary, need to save to get ID for file path if we want
        // paper_{id})
        // Alternatively, use a UUID or timestamp for uniqueness before DB save
        ResearchPaper paper = new ResearchPaper();
        paper.setTitle(metadata.getTitle());
        paper.setAuthorName(metadata.getAuthorName());
        paper.setAbstractText(metadata.getAbstractText());
        paper.setDepartment(department);
        paper.setSubmissionDate(metadata.getSubmissionDate());
        paper.setArchived(false);

        // We need a path. Pattern: {year}/{dept_slug}/filename
        String year = String.valueOf(metadata.getSubmissionDate().getYear());
        String deptSlug = department.getDepartmentName().toLowerCase().replaceAll("[^a-z0-9]", "_");
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".pdf";

        // To avoid collisions and because we don't have the ID yet, use timestamp +
        // random
        String filename = "paper_" + System.currentTimeMillis() + extension;
        String relativePath = String.format("%s/%s/%s", year, deptSlug, filename);

        // Save file
        fileStorageService.saveFile(file, relativePath);

        // Update entity with path and save
        paper.setFilePath(relativePath);
        ResearchPaper savedPaper = researchPaperRepository.save(paper);

        return researchPaperMapper.toDto(savedPaper);
    }

    private ResearchPaper getAndVerifyAdminAccess(Integer id, CustomUserPrincipal principal) {
        if (!RoleBasedAccess.isUserAdmin(principal)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Admin privileges required");
        }

        ResearchPaper paper = researchPaperRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Paper not found"));

        if (RoleBasedAccess.isUserDepartmentAdmin(principal)) {
            Integer userDeptId = principal.getDepartmentId();
            if (userDeptId == null || !userDeptId.equals(paper.getDepartment().getDepartmentId())) {
                throw new ApiException(ErrorCode.ACCESS_DENIED, "You do not have permission to manage this paper");
            }
        }

        return paper;
    }

    private void validateDownloadAccess(ResearchPaper paper, CustomUserPrincipal principal) {
        if (RoleBasedAccess.isUserSuperAdmin(principal)) {
            return;
        }

        if (RoleBasedAccess.isUserDepartmentAdmin(principal)) {
            if (principal.getDepartmentId() == null
                    || !principal.getDepartmentId().equals(paper.getDepartment().getDepartmentId())) {
                throw new ApiException(ErrorCode.ACCESS_DENIED, "You do not have access to files in this department");
            }
            return;
        }

        // Student/Teacher
        if (paper.getArchived()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_AVAILABLE, "Paper not available");
        }

        boolean hasAcceptedRequest = documentRequestRepository.existsByPaperPaperIdAndUserUserIdAndStatus(
                paper.getPaperId(), principal.getUserId(), RequestStatus.ACCEPTED);

        if (!hasAcceptedRequest) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "You do not have access to this file");
        }
    }
}
