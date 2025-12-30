package com.acd.researchrepo.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.acd.researchrepo.dto.external.model.ResearchPaperDto;
import com.acd.researchrepo.dto.external.papers.PaginatedResponse;
import com.acd.researchrepo.dto.external.papers.PaperUserRequestResponse;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.mapper.ResearchPaperMapper;
import com.acd.researchrepo.model.ResearchPaper;
import com.acd.researchrepo.repository.ResearchPaperRepository;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.spec.ResearchPaperSpec;
import com.acd.researchrepo.util.RoleBasedAccess;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ResearchPaperService {
    private final ResearchPaperRepository researchPaperRepository;
    private final ResearchPaperMapper researchPaperMapper;
    private final DocumentRequestService documentRequestService;

    public ResearchPaperService(
            ResearchPaperRepository researchPaperRepository,
            ResearchPaperMapper researchPaperMapper,
            DocumentRequestService documentRequestService) {
        this.researchPaperRepository = researchPaperRepository;
        this.researchPaperMapper = researchPaperMapper;
        this.documentRequestService = documentRequestService;
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

        if (RoleBasedAccess.isUserStudentOrTeacher(userPrincipal)) {
            archived = false;
        }

        // For department admins, override the departmentIds with their own department
        if (RoleBasedAccess.isUserDepartmentAdmin(userPrincipal)) {
            departmentIds = String.valueOf(userPrincipal.getDepartmentId());
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

    private String allowedSortBy(String sortBy) {
        if ("title".equalsIgnoreCase(sortBy))
            return "title";
        if ("authorName".equalsIgnoreCase(sortBy))
            return "authorName";
        return "submissionDate";
    }
}
