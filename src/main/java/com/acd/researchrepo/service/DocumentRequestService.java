package com.acd.researchrepo.service;

import java.util.List;
import java.util.Optional;

import com.acd.researchrepo.dto.external.model.UserDocumentRequestDto;
import com.acd.researchrepo.dto.external.papers.PaginatedResponse;
import com.acd.researchrepo.dto.external.papers.PaperUserRequestResponse;
import com.acd.researchrepo.dto.external.requests.AdminRequestResponse;
import com.acd.researchrepo.dto.external.requests.CreateRequestRequest;
import com.acd.researchrepo.dto.external.requests.CreateRequestResponse;
import com.acd.researchrepo.dto.external.requests.DocumentRequestSearchRequest;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.mapper.DocumentRequestMapper;
import com.acd.researchrepo.model.DocumentRequest;
import com.acd.researchrepo.model.RequestStatus;
import com.acd.researchrepo.model.ResearchPaper;
import com.acd.researchrepo.repository.DocumentRequestRepository;
import com.acd.researchrepo.repository.ResearchPaperRepository;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.spec.DocumentRequestSpec;
import com.acd.researchrepo.util.RoleBasedAccess;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentRequestService {
    private final DocumentRequestRepository documentRequestRepository;
    private final ResearchPaperRepository researchPaperRepository;
    private final DocumentRequestMapper documentRequestMapper;

    public DocumentRequestService(
            DocumentRequestRepository documentRequestRepository,
            ResearchPaperRepository researchPaperRepository,
            DocumentRequestMapper documentRequestMapper) {
        this.documentRequestRepository = documentRequestRepository;
        this.researchPaperRepository = researchPaperRepository;
        this.documentRequestMapper = documentRequestMapper;
    }

    public PaginatedResponse<UserDocumentRequestDto> getUserDocumentRequests(
            CustomUserPrincipal userPrincipal,
            DocumentRequestSearchRequest request) {

        if (!RoleBasedAccess.isUserStudentOrTeacher(userPrincipal)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        Specification<DocumentRequest> spec = DocumentRequestSpec.userRequestFilter(
                userPrincipal.getUserId(),
                request.getStatus(),
                request.getSearch());

        Page<DocumentRequest> requestPage = documentRequestRepository.findAll(spec, request.toPageable());

        return PaginatedResponse.fromPage(requestPage, documentRequestMapper::toDto);
    }

    @Transactional
    public CreateRequestResponse createRequest(
            CreateRequestRequest requestDto,
            CustomUserPrincipal userPrincipal) {
        if (!RoleBasedAccess.isUserStudentOrTeacher(userPrincipal)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        if (requestDto.getPaperId() == null || requestDto.getPaperId() <= 0) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Invalid paper ID");
        }

        Optional<ResearchPaper> paperOpt = researchPaperRepository.findById(requestDto.getPaperId());

        List<DocumentRequest> existingActiveRequests = documentRequestRepository
                .findByUserIdAndPaperIdAndActiveStatus(userPrincipal.getUserId(), requestDto.getPaperId());
        if (!existingActiveRequests.isEmpty()) {
            throw new ApiException(ErrorCode.DUPLICATE_REQUEST, "Duplicate active request exists");
        }

        DocumentRequest newRequest = new DocumentRequest();
        newRequest.setUser(userPrincipal.getUser());
        newRequest.setPaper(paperOpt.get());
        newRequest.setStatus(RequestStatus.PENDING);

        DocumentRequest savedRequest = documentRequestRepository.save(newRequest);

        return CreateRequestResponse.builder()
                .requestId(savedRequest.getRequestId())
                .build();
    }

    @Transactional
    public void deleteRequest(Integer requestId, CustomUserPrincipal userPrincipal) {
        if (!RoleBasedAccess.isUserStudentOrTeacher(userPrincipal)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        if (requestId == null || requestId <= 0) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Invalid request ID");
        }

        Optional<DocumentRequest> requestOpt = documentRequestRepository.findByIdAndUserId(requestId,
                userPrincipal.getUserId());
        if (requestOpt.isEmpty()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Request not found");
        }

        DocumentRequest request = requestOpt.get();

        if (request.getStatus() == RequestStatus.REJECTED || request.getStatus() == RequestStatus.PENDING) {
            documentRequestRepository.delete(request);
        } else {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Not allowed to delete this request");
        }
    }

    public PaperUserRequestResponse getUserRequestForPaper(Integer paperId, CustomUserPrincipal userPrincipal) {
        if (!RoleBasedAccess.isUserStudentOrTeacher(userPrincipal)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        if (paperId == null || paperId <= 0) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Invalid paper ID");
        }

        Optional<ResearchPaper> paperOpt = researchPaperRepository.findById(paperId);
        if (paperOpt.isEmpty() || paperOpt.get().getArchived()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Paper not found");
        }

        // Find the user's request for this paper
        Optional<DocumentRequest> requestOpt = documentRequestRepository.findByUserIdAndPaperId(
                userPrincipal.getUserId(),
                paperId);

        if (requestOpt.isEmpty()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "No request found for this paper/user");
        }

        DocumentRequest request = requestOpt.get();
        return documentRequestMapper.toPaperUserRequestResponse(request);
    }

    public PaginatedResponse<AdminRequestResponse> getAdminRequests(
            DocumentRequestSearchRequest request,
            CustomUserPrincipal userPrincipal) {

        if (!RoleBasedAccess.isUserAdmin(userPrincipal)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        // Determine target department
        Integer filterDepartmentId = RoleBasedAccess.isUserSuperAdmin(userPrincipal)
                ? request.getDepartmentId()
                : getUserDepartmentIdIfDepartmentAdmin(userPrincipal);

        Specification<DocumentRequest> spec = DocumentRequestSpec.adminRequestFilter(
                filterDepartmentId,
                request.getStatus(),
                request.getSearch());

        Page<DocumentRequest> requestPage = documentRequestRepository.findAll(spec, request.toPageable());

        return PaginatedResponse.fromPage(requestPage, documentRequestMapper::toAdminDto);
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

    @Transactional
    public AdminRequestResponse acceptRequest(Integer requestId, CustomUserPrincipal userPrincipal) {
        // Authorization: must be admin
        if (!RoleBasedAccess.isUserAdmin(userPrincipal)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Admin privileges required");
        }

        // Find the request
        DocumentRequest request = documentRequestRepository.findById(requestId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Request not found"));

        // Check if request is in PENDING status
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ApiException(ErrorCode.REQUEST_ALREADY_FINAL, "Request is already in a terminal state");
        }

        // Check if paper is archived
        if (request.getPaper().getArchived()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Paper not found");
        }

        // For DEPARTMENT_ADMIN, verify department access
        if (RoleBasedAccess.isUserDepartmentAdmin(userPrincipal)) {
            Integer adminDepartmentId = userPrincipal.getDepartmentId();
            Integer paperDepartmentId = request.getPaper().getDepartment().getDepartmentId();
            if (!adminDepartmentId.equals(paperDepartmentId)) {
                throw new ApiException(ErrorCode.ACCESS_DENIED,
                        "You do not have permission to approve requests for this department");
            }
        }

        // Update request status
        request.setStatus(RequestStatus.ACCEPTED);
        DocumentRequest savedRequest = documentRequestRepository.save(request);

        return documentRequestMapper.toAdminDto(savedRequest);
    }

    @Transactional
    public AdminRequestResponse rejectRequest(Integer requestId, String reason, CustomUserPrincipal userPrincipal) {
        // Authorization: must be admin
        if (!RoleBasedAccess.isUserAdmin(userPrincipal)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Admin privileges required");
        }

        // Find the request
        DocumentRequest request = documentRequestRepository.findById(requestId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Request not found"));

        // Check if request is already REJECTED (terminal state)
        if (request.getStatus() == RequestStatus.REJECTED) {
            throw new ApiException(ErrorCode.REQUEST_ALREADY_FINAL, "Request is already in a terminal state");
        }

        // Check if paper is archived
        if (request.getPaper().getArchived()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Paper not found");
        }

        // For DEPARTMENT_ADMIN, verify department access
        if (RoleBasedAccess.isUserDepartmentAdmin(userPrincipal)) {
            Integer adminDepartmentId = userPrincipal.getDepartmentId();
            Integer paperDepartmentId = request.getPaper().getDepartment().getDepartmentId();
            if (!adminDepartmentId.equals(paperDepartmentId)) {
                throw new ApiException(ErrorCode.ACCESS_DENIED,
                        "You do not have permission to reject requests for this department");
            }
        }

        // Update request status and reason
        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(reason);
        DocumentRequest savedRequest = documentRequestRepository.save(request);

        return documentRequestMapper.toAdminDto(savedRequest);
    }
}
