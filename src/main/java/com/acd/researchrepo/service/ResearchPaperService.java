package com.acd.researchrepo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.acd.researchrepo.dto.external.model.ResearchPaperDto;
import com.acd.researchrepo.dto.external.papers.PaginatedResponse;
import com.acd.researchrepo.dto.external.papers.PaperUserRequestResponse;
import com.acd.researchrepo.dto.external.papers.ResearchPaperSearchRequest;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.mapper.ResearchPaperMapper;
import com.acd.researchrepo.model.DocumentRequest;
import com.acd.researchrepo.model.RequestStatus;
import com.acd.researchrepo.model.ResearchPaper;
import com.acd.researchrepo.repository.DocumentRequestRepository;
import com.acd.researchrepo.repository.ResearchPaperRepository;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.spec.ResearchPaperSpec;
import com.acd.researchrepo.util.RoleBasedAccess;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResearchPaperService {
    private final ResearchPaperRepository researchPaperRepository;
    private final DocumentRequestRepository documentRequestRepository;
    private final ResearchPaperMapper researchPaperMapper;
    private final DocumentRequestService documentRequestService;

    public ResearchPaperService(
            ResearchPaperRepository researchPaperRepository,
            DocumentRequestRepository documentRequestRepository,
            ResearchPaperMapper researchPaperMapper,
            DocumentRequestService documentRequestService) {
        this.researchPaperRepository = researchPaperRepository;
        this.documentRequestRepository = documentRequestRepository;
        this.researchPaperMapper = researchPaperMapper;
        this.documentRequestService = documentRequestService;
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
    public void unarchivePaper(Integer id, CustomUserPrincipal principal) {
        ResearchPaper paper = getAndVerifyAdminAccess(id, principal);

        paper.setArchived(false);
        paper.setArchivedAt(null);
        researchPaperRepository.save(paper);
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
}
