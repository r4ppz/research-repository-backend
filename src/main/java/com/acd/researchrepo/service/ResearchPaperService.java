package com.acd.researchrepo.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.acd.researchrepo.dto.external.papers.PaginatedResponseDto;
import com.acd.researchrepo.dto.external.papers.ResearchPaperDto;
import com.acd.researchrepo.mapper.ResearchPaperMapper;
import com.acd.researchrepo.model.ResearchPaper;
import com.acd.researchrepo.model.enums.UserRole;
import com.acd.researchrepo.repository.ResearchPaperRepository;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.specification.ResearchPaperSpecification;

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

    public ResearchPaperService(
            ResearchPaperRepository researchPaperRepository,
            ResearchPaperMapper researchPaperMapper) {
        this.researchPaperRepository = researchPaperRepository;
        this.researchPaperMapper = researchPaperMapper;
    }

    public PaginatedResponseDto<ResearchPaperDto> getPapers(
            String searchTerm,
            List<Integer> departmentIds,
            Integer year,
            Boolean archived,
            String sortBy,
            String sortOrder,
            int page,
            int size,
            CustomUserPrincipal userPrincipal) {

        UserRole userRole = userPrincipal.getRole();

        if (userRole == UserRole.STUDENT || userRole == UserRole.TEACHER) {
            archived = false;
        }

        if (userRole == UserRole.DEPARTMENT_ADMIN) {
            departmentIds = List.of(userPrincipal.getDepartmentId());
        }

        // Sanitize sortBy and sortOrder against allowed fields
        Sort sort = Sort.by((sortOrder != null && sortOrder.equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC, allowedSortBy(sortBy));

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<ResearchPaper> spec = ResearchPaperSpecification
                .build(searchTerm, departmentIds, year, archived);

        Page<ResearchPaper> paperPage = researchPaperRepository.findAll(spec, pageable);

        List<ResearchPaperDto> researchPaperDtos = paperPage
                .getContent()
                .stream()
                .map(researchPaperMapper::toDto)
                .collect(Collectors.toList());

        return PaginatedResponseDto.<ResearchPaperDto>builder()
                .content(researchPaperDtos)
                .totalElements((int) paperPage.getTotalElements())
                .totalPages(paperPage.getTotalPages())
                .number(paperPage.getNumber())
                .size(paperPage.getSize())
                .build();
    }

    public List<Integer> getAvailableYears(CustomUserPrincipal user) {
        UserRole role = user.getRole();
        List<ResearchPaper> papers;

        if (role == UserRole.DEPARTMENT_ADMIN) {
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
                    if (role == UserRole.STUDENT)
                        return !paper.getArchived();
                    // TEACHER, DEPT_ADMIN, SUPER_ADMIN: allow all in-scope
                    if (role == UserRole.DEPARTMENT_ADMIN)
                        return true;
                    return true;
                })
                .map(paper -> paper.getSubmissionDate().getYear())
                .distinct()
                .sorted(Comparator.reverseOrder()) // Descending order
                .collect(Collectors.toList());
    }

    private String allowedSortBy(String sortBy) {
        if ("title".equalsIgnoreCase(sortBy))
            return "title";
        if ("authorName".equalsIgnoreCase(sortBy))
            return "authorName";
        return "submissionDate";
    }
}
