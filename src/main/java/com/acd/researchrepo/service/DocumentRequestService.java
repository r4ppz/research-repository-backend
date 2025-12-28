package com.acd.researchrepo.service;

import java.util.List;
import java.util.stream.Collectors;

import com.acd.researchrepo.dto.external.requests.UserDocumentRequestDto;
import com.acd.researchrepo.dto.external.requests.UserDocumentRequestsResponse;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.mapper.DocumentRequestMapper;
import com.acd.researchrepo.model.DocumentRequest;
import com.acd.researchrepo.repository.DocumentRequestRepository;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.util.RoleBasedAccess;

import org.springframework.stereotype.Service;

@Service
public class DocumentRequestService {
    private final DocumentRequestRepository documentRequestRepository;
    private final DocumentRequestMapper documentRequestMapper;

    public DocumentRequestService(
            DocumentRequestRepository documentRequestRepository,
            DocumentRequestMapper documentRequestMapper) {
        this.documentRequestRepository = documentRequestRepository;
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
}
