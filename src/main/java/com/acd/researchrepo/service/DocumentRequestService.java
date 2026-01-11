package com.acd.researchrepo.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.acd.researchrepo.dto.external.model.ResearchPaperDto;
import com.acd.researchrepo.dto.external.model.UserDocumentRequestDto;
import com.acd.researchrepo.dto.external.model.UserDto;
import com.acd.researchrepo.dto.external.papers.PaginatedResponse;
import com.acd.researchrepo.dto.external.papers.PaperUserRequestResponse;
import com.acd.researchrepo.dto.external.requests.AdminRequestDetailResponse;
import com.acd.researchrepo.dto.external.requests.CreateRequestRequest;
import com.acd.researchrepo.dto.external.requests.CreateRequestResponse;
import com.acd.researchrepo.dto.external.requests.UserDocumentRequestsResponse;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.mapper.ResearchPaperMapper;
import com.acd.researchrepo.mapper.UserMapper;
import com.acd.researchrepo.model.DocumentRequest;
import com.acd.researchrepo.model.RequestStatus;
import com.acd.researchrepo.model.ResearchPaper;
import com.acd.researchrepo.repository.DocumentRequestRepository;
import com.acd.researchrepo.repository.ResearchPaperRepository;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.spec.DocumentRequestSpec;
import com.acd.researchrepo.util.RequestParamValidator;
import com.acd.researchrepo.util.RoleBasedAccess;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

@Service
public class DocumentRequestService {
    private final DocumentRequestRepository documentRequestRepository;
    private final ResearchPaperRepository researchPaperRepository;
    private final ResearchPaperMapper researchPaperMapper;
    private final UserMapper userMapper;

    public DocumentRequestService(
            DocumentRequestRepository documentRequestRepository,
            ResearchPaperRepository researchPaperRepository,
            ResearchPaperMapper researchPaperMapper,
            UserMapper userMapper) {
        this.documentRequestRepository = documentRequestRepository;
        this.researchPaperRepository = researchPaperRepository;
        this.researchPaperMapper = researchPaperMapper;
        this.userMapper = userMapper;
    }

    public PaginatedResponse<UserDocumentRequestDto> getUserDocumentRequests(
            CustomUserPrincipal userPrincipal,
            String statusStr,
            String search,
            String sortBy,
            String sortOrder,
            int page,
            int size) {

        if (!RoleBasedAccess.isUserStudentOrTeacher(userPrincipal)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        // Parse status filter
        List<RequestStatus> statuses = parseStatusFilter(statusStr);
        RequestParamValidator.validatePagination(page, size);
        RequestParamValidator.validateSortParams(sortBy, sortOrder, "createdAt", "paper.title", "status");

        // Sanitize sortBy and sortOrder against allowed fields
        Sort sort = Sort.by((sortOrder != null && sortOrder.equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC, allowedSortBy(sortBy));

        Pageable pageable = PageRequest.of(page, size, sort);

        // Build specification for filtering
        Specification<DocumentRequest> spec = createRequestFilterSpec(
                userPrincipal.getUserId(), statuses, search);

        Page<DocumentRequest> requestPage = documentRequestRepository.findAll(spec, pageable);

        List<UserDocumentRequestDto> requestDtos = requestPage
                .getContent()
                .stream()
                .map(this::mapToUserDocumentRequestDto)
                .collect(Collectors.toList());

        return PaginatedResponse.<UserDocumentRequestDto>builder()
                .content(requestDtos)
                .totalElements((int) requestPage.getTotalElements())
                .totalPages(requestPage.getTotalPages())
                .number(requestPage.getNumber())
                .size(requestPage.getSize())
                .build();
    }

    private List<RequestStatus> parseStatusFilter(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return null;
        }

        // Validate status using the utility
        RequestParamValidator.validateStatus(statusStr, "PENDING", "ACCEPTED", "REJECTED");

        String[] statusArray = statusStr.split(",");
        List<RequestStatus> statuses = java.util.Arrays.stream(statusArray)
                .map(String::trim)
                .map(this::parseStatus)
                .toList();

        return statuses.isEmpty() ? null : statuses;
    }

    private RequestStatus parseStatus(String status) {
        try {
            return RequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.INVALID_REQUEST,
                    "Invalid status. Must be PENDING, ACCEPTED, or REJECTED");
        }
    }

    private String allowedSortBy(String sortBy) {
        if ("createdAt".equalsIgnoreCase(sortBy))
            return "createdAt";
        if ("paper.title".equalsIgnoreCase(sortBy))
            return "paper.title";
        if ("status".equalsIgnoreCase(sortBy))
            return "status";
        return "createdAt"; // default
    }

    private Specification<DocumentRequest> createRequestFilterSpec(
            Integer userId, List<RequestStatus> statuses, String search) {

        return (root, query, cb) -> {
            // Always filter by user ID
            var predicates = List.of(
                    cb.equal(root.get("user").get("userId"), userId));

            var predicate = cb.and(predicates.toArray(new Predicate[0]));

            // Add status filter if provided
            if (statuses != null && !statuses.isEmpty()) {
                predicate = cb.and(predicate, root.get("status").in(statuses));
            }

            // Add search filter if provided
            if (search != null && !search.isEmpty()) {
                var searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("paper").get("title")), "%" + search.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("paper").get("authorName")), "%" + search.toLowerCase() + "%"));
                predicate = cb.and(predicate, searchPredicate);
            }

            // Ensure paper is not archived
            predicate = cb.and(predicate, cb.isFalse(root.get("paper").get("archived")));

            return predicate;
        };
    }

    private UserDocumentRequestDto mapToUserDocumentRequestDto(DocumentRequest request) {
        ResearchPaperDto paperDto = researchPaperMapper.toDto(request.getPaper());

        return UserDocumentRequestDto.builder()
                .requestId(request.getRequestId())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .paper(paperDto)
                .build();
    }

    public UserDocumentRequestsResponse getUserDocumentRequests(CustomUserPrincipal userPrincipal) {
        if (!RoleBasedAccess.isUserStudentOrTeacher(userPrincipal)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        // Call the new paginated method with default parameters
        PaginatedResponse<UserDocumentRequestDto> paginatedResponse = getUserDocumentRequests(
                userPrincipal, null, null, "createdAt", "desc", 0, 20);

        return UserDocumentRequestsResponse.builder()
                .requests(paginatedResponse.getContent())
                .build();
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
        if (paperOpt.isEmpty() || paperOpt.get().getArchived()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Paper not found");
        }

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

        return PaperUserRequestResponse.builder()
                .requestId(request.getRequestId())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

    public PaginatedResponse<AdminRequestDetailResponse> getAdminRequests(
            Integer departmentId,
            Integer userDepartmentId,
            List<RequestStatus> statuses,
            String search,
            int page,
            int size,
            String sortBy,
            String sortOrder,
            CustomUserPrincipal principal) {

        if (!RoleBasedAccess.isUserAdmin(principal)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        Integer filterDepartmentId = null;
        if (RoleBasedAccess.isUserSuperAdmin(principal)) {
            filterDepartmentId = departmentId;
        } else if (RoleBasedAccess.isUserDepartmentAdmin(principal)) {
            filterDepartmentId = userDepartmentId;
        }

        Sort sort = createSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<DocumentRequest> spec = DocumentRequestSpec
                .adminRequestFilter(filterDepartmentId, statuses, search);

        Page<DocumentRequest> requestPage = documentRequestRepository.findAll(spec, pageable);

        List<AdminRequestDetailResponse> content = requestPage
                .getContent()
                .stream()
                .map(this::mapToAdminRequestDetailResponse)
                .collect(Collectors.toList());

        return PaginatedResponse.<AdminRequestDetailResponse>builder()
                .content(content)
                .totalElements((int) requestPage.getTotalElements())
                .totalPages(requestPage.getTotalPages())
                .number(requestPage.getNumber())
                .size(requestPage.getSize())
                .build();
    }

    private AdminRequestDetailResponse mapToAdminRequestDetailResponse(DocumentRequest request) {
        ResearchPaperDto paperDto = researchPaperMapper.toDto(request.getPaper());
        UserDto userDto = userMapper.toDto(request.getUser());

        return AdminRequestDetailResponse
                .builder()
                .requestId(request.getRequestId())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .user(userDto)
                .paper(paperDto)
                .build();
    }

    private Sort createSort(String sortBy, String sortOrder) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "createdAt"; // default
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // Map the sort fields to actual entity fields
        switch (sortBy) {
            case "createdAt":
                return Sort.by(direction, "createdAt");
            case "status":
                return Sort.by(direction, "status");
            case "paper.title":
                return Sort.by(direction, "paper.title");
            case "userId":
                return Sort.by(direction, "user.userId");
            default:
                return Sort.by(direction, "createdAt");
        }
    }
}
