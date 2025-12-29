package com.acd.researchrepo.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.acd.researchrepo.dto.external.model.UserDocumentRequestDto;
import com.acd.researchrepo.dto.external.papers.PaperUserRequestResponse;
import com.acd.researchrepo.dto.external.requests.CreateRequestRequest;
import com.acd.researchrepo.dto.external.requests.CreateRequestResponse;
import com.acd.researchrepo.dto.external.requests.UserDocumentRequestsResponse;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.mapper.DocumentRequestMapper;
import com.acd.researchrepo.model.DocumentRequest;
import com.acd.researchrepo.model.RequestStatus;
import com.acd.researchrepo.model.ResearchPaper;
import com.acd.researchrepo.repository.DocumentRequestRepository;
import com.acd.researchrepo.repository.ResearchPaperRepository;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.util.RoleBasedAccess;

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

    public UserDocumentRequestsResponse getUserDocumentRequests(CustomUserPrincipal userPrincipal) {
        if (!RoleBasedAccess.isUserStudentOrTeacher(userPrincipal)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        List<DocumentRequest> userRequests = documentRequestRepository
                .findByUserIdAndPaperNotArchived(userPrincipal.getUserId());

        List<UserDocumentRequestDto> requestDtos = userRequests
                .stream()
                .map(documentRequestMapper::toDto)
                .collect(Collectors.toList());

        return UserDocumentRequestsResponse.builder()
                .requests(requestDtos)
                .build();
    }

    @Transactional
    public CreateRequestResponse createRequest(CreateRequestRequest requestDto,
            CustomUserPrincipal userPrincipal) {
        if (!RoleBasedAccess.isUserStudentOrTeacher(userPrincipal)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        if (requestDto.getPaperId() == null || requestDto.getPaperId() <= 0) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Invalid paper ID");
        }

        // Check if paper exists and is not archived
        Optional<ResearchPaper> paperOpt = researchPaperRepository.findById(requestDto.getPaperId());
        if (paperOpt.isEmpty() || paperOpt.get().getArchived()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Paper not found");
        }

        // Check if user already has a PENDING or ACCEPTED request for this paper
        List<DocumentRequest> existingActiveRequests = documentRequestRepository
                .findByUserIdAndPaperIdAndActiveStatus(userPrincipal.getUserId(), requestDto.getPaperId());
        if (!existingActiveRequests.isEmpty()) {
            throw new ApiException(ErrorCode.DUPLICATE_REQUEST, "Duplicate active request exists");
        }

        // Create new request
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

        // Find the request and check if it belongs to the user
        Optional<DocumentRequest> requestOpt = documentRequestRepository.findByIdAndUserId(requestId,
                userPrincipal.getUserId());
        if (requestOpt.isEmpty()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Request not found");
        }

        DocumentRequest request = requestOpt.get();

        // Check if the request can be deleted (REJECTED or PENDING)
        if (request.getStatus() == RequestStatus.REJECTED || request.getStatus() == RequestStatus.PENDING) {
            documentRequestRepository.delete(request);
        } else {
            // Only REJECTED or PENDING requests can be deleted by the user
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
                userPrincipal.getUserId(), paperId);

        if (requestOpt.isEmpty()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "No request found for this paper/user");
        }

        DocumentRequest request = requestOpt.get();

        return PaperUserRequestResponse.builder()
                .requestId(request.getRequestId())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
