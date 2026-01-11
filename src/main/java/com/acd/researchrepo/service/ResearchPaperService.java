package com.acd.researchrepo.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.acd.researchrepo.dto.external.model.ResearchPaperDto;
import com.acd.researchrepo.dto.external.papers.PaginatedResponse;
import com.acd.researchrepo.dto.external.papers.PaperCreateRequest;
import com.acd.researchrepo.dto.external.papers.PaperUpdateRequest;
import com.acd.researchrepo.dto.external.papers.PaperUserRequestResponse;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.mapper.ResearchPaperMapper;
import com.acd.researchrepo.model.ResearchPaper;
import com.acd.researchrepo.repository.DepartmentRepository;
import com.acd.researchrepo.repository.ResearchPaperRepository;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.spec.ResearchPaperSpec;
import com.acd.researchrepo.util.FileStorageUtil;
import com.acd.researchrepo.util.RoleBasedAccess;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResearchPaperService {
    private final ResearchPaperRepository researchPaperRepository;
    private final ResearchPaperMapper researchPaperMapper;
    private final DocumentRequestService documentRequestService;
    private final DepartmentRepository departmentRepository;
    private final FileStorageUtil fileStorageUtil;

    public ResearchPaperService(
            ResearchPaperRepository researchPaperRepository,
            ResearchPaperMapper researchPaperMapper,
            DocumentRequestService documentRequestService,
            DepartmentRepository departmentRepository,
            FileStorageUtil fileStorageUtil) {
        this.researchPaperRepository = researchPaperRepository;
        this.researchPaperMapper = researchPaperMapper;
        this.documentRequestService = documentRequestService;
        this.departmentRepository = departmentRepository;
        this.fileStorageUtil = fileStorageUtil;
    }

    public PaginatedResponse<ResearchPaperDto> getPapers(
            String searchTerm,
            String departmentIds,
            String years,
            Boolean archived,
            String sortBy,
            String sortOrder,
            int page,
            int size,
            CustomUserPrincipal userPrincipal) {

        if (RoleBasedAccess.isUserStudent(userPrincipal)) {
            archived = false;
        }

        // Sanitize sortBy and sortOrder against allowed fields
        Sort sort = Sort.by((sortOrder != null && sortOrder.equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC, allowedSortBy(sortBy));

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<ResearchPaper> spec = ResearchPaperSpec
                .build(searchTerm, departmentIds, years, archived);

        Page<ResearchPaper> paperPage = researchPaperRepository.findAll(spec, pageable);

        List<ResearchPaperDto> researchPaperDtos = paperPage
                .getContent()
                .stream()
                .map(researchPaperMapper::toDto)
                .collect(Collectors.toList());

        return PaginatedResponse.<ResearchPaperDto>builder()
                .content(researchPaperDtos)
                .totalElements((int) paperPage.getTotalElements())
                .totalPages(paperPage.getTotalPages())
                .number(paperPage.getNumber())
                .size(paperPage.getSize())
                .build();
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
        List<ResearchPaper> papers;

        if (RoleBasedAccess.isUserDepartmentAdmin(user)) {
            Integer deptId = user.getDepartmentId();
            if (deptId == null)
                return List.of();
            papers = researchPaperRepository
                    .findAll((root, query, cb) -> cb.equal(root.get("department").get("departmentId"), deptId));
        } else {
            papers = researchPaperRepository.findAll();
        }

        return papers.stream()
                .filter(paper -> {
                    if (RoleBasedAccess.isUserStudent(user))
                        return !paper.getArchived();
                    if (RoleBasedAccess.isUserDepartmentAdmin(user))
                        return true;
                    return true;
                })
                .map(paper -> paper.getSubmissionDate().getYear())
                .distinct()
                .sorted(java.util.Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    public PaperUserRequestResponse getUserRequestForPaper(Integer paperId, CustomUserPrincipal userPrincipal) {
        return documentRequestService.getUserRequestForPaper(paperId, userPrincipal);
    }

    public PaginatedResponse<ResearchPaperDto> getAdminPapers(
            String searchTerm,
            String departmentIds,
            Boolean archived,
            String years,
            String sortBy,
            String sortOrder,
            int page,
            int size,
            CustomUserPrincipal userPrincipal) {

        // Sanitize sortBy and sortOrder against allowed fields
        Sort sort = Sort.by((sortOrder != null && sortOrder.equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC, allowedSortByForAdmin(sortBy));

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<ResearchPaper> spec = ResearchPaperSpec
                .build(searchTerm, departmentIds, years, archived);

        Page<ResearchPaper> paperPage = researchPaperRepository.findAll(spec, pageable);

        List<ResearchPaperDto> researchPaperDtos = paperPage
                .getContent()
                .stream()
                .map(researchPaperMapper::toDto)
                .collect(Collectors.toList());

        return PaginatedResponse.<ResearchPaperDto>builder()
                .content(researchPaperDtos)
                .totalElements((int) paperPage.getTotalElements())
                .totalPages(paperPage.getTotalPages())
                .number(paperPage.getNumber())
                .size(paperPage.getSize())
                .build();
    }

    private String allowedSortBy(String sortBy) {
        if ("title".equalsIgnoreCase(sortBy))
            return "title";
        if ("authorName".equalsIgnoreCase(sortBy))
            return "authorName";
        return "submissionDate";
    }

    public ResearchPaperDto getAdminPaperById(Integer id, CustomUserPrincipal principal) {
        Optional<ResearchPaper> paperOpt = researchPaperRepository.findById(id);

        if (paperOpt.isEmpty()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Paper not found");
        }

        ResearchPaper paper = paperOpt.get();

        // Authorization check: DEPARTMENT_ADMIN can only access papers from their
        // department
        if (RoleBasedAccess.isUserDepartmentAdmin(principal)) {
            Integer userDepartmentId = principal.getDepartmentId();
            if (userDepartmentId == null || !userDepartmentId.equals(paper.getDepartment().getDepartmentId())) {
                throw new ApiException(ErrorCode.ACCESS_DENIED, "You can only manage papers within your department.");
            }
        }

        return researchPaperMapper.toDto(paper);
    }

    public ResearchPaperDto createPaper(PaperCreateRequest createRequest, MultipartFile file,
            CustomUserPrincipal principal) {
        // Authorization check: DEPARTMENT_ADMIN must provide their own department ID
        if (RoleBasedAccess.isUserDepartmentAdmin(principal)) {
            Integer userDepartmentId = principal.getDepartmentId();
            if (userDepartmentId == null) {
                throw new ApiException(ErrorCode.ACCESS_DENIED, "Department admin not assigned to a department");
            }
            if (createRequest.getDepartmentId() == null || !createRequest.getDepartmentId().equals(userDepartmentId)) {
                throw new ApiException(ErrorCode.ACCESS_DENIED, "You can only manage papers within your department.");
            }
        }

        // Validate required fields
        validatePaperCreateRequest(createRequest);

        // TODO: Handle file upload and storage
        // For now, we'll use a placeholder file path
        String filePath = saveUploadedFile(file);

        // Fetch the department from the database
        com.acd.researchrepo.model.Department dept = departmentRepository.findById(createRequest.getDepartmentId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Department not found"));

        ResearchPaper paper = new ResearchPaper();
        paper.setTitle(createRequest.getTitle());
        paper.setAuthorName(createRequest.getAuthorName());
        paper.setAbstractText(createRequest.getAbstractText());
        paper.setSubmissionDate(createRequest.getSubmissionDate());
        paper.setFilePath(filePath);
        paper.setDepartment(dept);

        ResearchPaper savedPaper = researchPaperRepository.save(paper);
        return researchPaperMapper.toDto(savedPaper);
    }

    public ResearchPaperDto updatePaper(Integer id, PaperUpdateRequest updateRequest, CustomUserPrincipal principal) {
        Optional<ResearchPaper> paperOpt = researchPaperRepository.findById(id);
        if (paperOpt.isEmpty()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Paper not found");
        }

        ResearchPaper paper = paperOpt.get();

        // Authorization check: DEPARTMENT_ADMIN can only update papers from their
        // department
        if (RoleBasedAccess.isUserDepartmentAdmin(principal)) {
            Integer userDepartmentId = principal.getDepartmentId();
            if (userDepartmentId == null || !userDepartmentId.equals(paper.getDepartment().getDepartmentId())) {
                throw new ApiException(ErrorCode.ACCESS_DENIED, "You can only manage papers within your department.");
            }
        }

        // Validate required fields
        validatePaperUpdateRequest(updateRequest);

        // Update fields
        paper.setTitle(updateRequest.getTitle());
        paper.setAuthorName(updateRequest.getAuthorName());
        paper.setAbstractText(updateRequest.getAbstractText());
        paper.setSubmissionDate(updateRequest.getSubmissionDate());

        // If department is being changed, check authorization and update
        if (updateRequest.getDepartmentId() != null
                && !updateRequest.getDepartmentId().equals(paper.getDepartment().getDepartmentId())) {
            if (RoleBasedAccess.isUserDepartmentAdmin(principal)) {
                Integer userDepartmentId = principal.getDepartmentId();
                if (!updateRequest.getDepartmentId().equals(userDepartmentId)) {
                    throw new ApiException(ErrorCode.ACCESS_DENIED,
                            "You can only manage papers within your department.");
                }
            }
            // Fetch and update department
            com.acd.researchrepo.model.Department newDept = departmentRepository
                    .findById(updateRequest.getDepartmentId())
                    .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Department not found"));
            paper.setDepartment(newDept);
        }

        ResearchPaper updatedPaper = researchPaperRepository.save(paper);
        return researchPaperMapper.toDto(updatedPaper);
    }

    public ResearchPaperDto archivePaper(Integer id, CustomUserPrincipal principal) {
        Optional<ResearchPaper> paperOpt = researchPaperRepository.findById(id);
        if (paperOpt.isEmpty()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Paper not found");
        }

        ResearchPaper paper = paperOpt.get();

        // Authorization check: DEPARTMENT_ADMIN can only archive papers from their
        // department
        if (RoleBasedAccess.isUserDepartmentAdmin(principal)) {
            Integer userDepartmentId = principal.getDepartmentId();
            if (userDepartmentId == null || !userDepartmentId.equals(paper.getDepartment().getDepartmentId())) {
                throw new ApiException(ErrorCode.ACCESS_DENIED, "You can only manage papers within your department.");
            }
        }

        paper.setArchived(true);
        paper.setArchivedAt(java.time.LocalDateTime.now());

        ResearchPaper archivedPaper = researchPaperRepository.save(paper);
        return researchPaperMapper.toDto(archivedPaper);
    }

    public ResearchPaperDto unarchivePaper(Integer id, CustomUserPrincipal principal) {
        Optional<ResearchPaper> paperOpt = researchPaperRepository.findById(id);
        if (paperOpt.isEmpty()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Paper not found");
        }

        ResearchPaper paper = paperOpt.get();

        // Authorization check: DEPARTMENT_ADMIN can only unarchive papers from their
        // department
        if (RoleBasedAccess.isUserDepartmentAdmin(principal)) {
            Integer userDepartmentId = principal.getDepartmentId();
            if (userDepartmentId == null || !userDepartmentId.equals(paper.getDepartment().getDepartmentId())) {
                throw new ApiException(ErrorCode.ACCESS_DENIED, "You can only manage papers within your department.");
            }
        }

        paper.setArchived(false);
        paper.setArchivedAt(null);

        ResearchPaper unarchivedPaper = researchPaperRepository.save(paper);
        return researchPaperMapper.toDto(unarchivedPaper);
    }

    public void deletePaper(Integer id, CustomUserPrincipal principal) {
        Optional<ResearchPaper> paperOpt = researchPaperRepository.findById(id);
        if (paperOpt.isEmpty()) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Paper not found");
        }

        ResearchPaper paper = paperOpt.get();

        // Authorization check: DEPARTMENT_ADMIN can only delete papers from their
        // department
        if (RoleBasedAccess.isUserDepartmentAdmin(principal)) {
            Integer userDepartmentId = principal.getDepartmentId();
            if (userDepartmentId == null || !userDepartmentId.equals(paper.getDepartment().getDepartmentId())) {
                throw new ApiException(ErrorCode.ACCESS_DENIED, "You can only manage papers within your department.");
            }
        }

        // Delete the physical file from storage
        deletePhysicalFile(paper.getFilePath());

        researchPaperRepository.delete(paper);
    }

    public PaperCreateRequest parsePaperCreateRequest(String metadataJson) {
        try {
            // Using Jackson for JSON parsing
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return objectMapper.readValue(metadataJson, PaperCreateRequest.class);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "The metadata part must be valid JSON.");
        }
    }

    private void validatePaperCreateRequest(PaperCreateRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Title is required.");
        }
        if (request.getAuthorName() == null || request.getAuthorName().trim().isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Author name is required.");
        }
        if (request.getAbstractText() == null || request.getAbstractText().trim().isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Abstract text is required.");
        }
        if (request.getDepartmentId() == null || request.getDepartmentId() <= 0) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Department ID is required.");
        }
        if (request.getSubmissionDate() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Submission date is required.");
        }
    }

    private void validatePaperUpdateRequest(PaperUpdateRequest request) {
        if (request.getTitle() != null && request.getTitle().trim().isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Title cannot be empty.");
        }
        if (request.getAuthorName() != null && request.getAuthorName().trim().isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Author name cannot be empty.");
        }
        if (request.getAbstractText() != null && request.getAbstractText().trim().isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Abstract text cannot be empty.");
        }
        if (request.getDepartmentId() != null && request.getDepartmentId() <= 0) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Department ID must be positive.");
        }
        if (request.getSubmissionDate() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Submission date is required.");
        }
    }

    private String saveUploadedFile(MultipartFile file) {
        return fileStorageUtil.storeFile(file);
    }

    private void deletePhysicalFile(String filePath) {
        fileStorageUtil.deleteFile(filePath);
    }

    private String allowedSortByForAdmin(String sortBy) {
        if ("title".equalsIgnoreCase(sortBy))
            return "title";
        if ("authorName".equalsIgnoreCase(sortBy))
            return "authorName";
        if ("submissionDate".equalsIgnoreCase(sortBy))
            return "submissionDate";
        if ("createdAt".equalsIgnoreCase(sortBy))
            return "createdAt";
        if ("updatedAt".equalsIgnoreCase(sortBy))
            return "updatedAt";
        return "submissionDate";
    }

}
