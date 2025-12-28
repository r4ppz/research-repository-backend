package com.acd.researchrepo.service;

import java.util.List;
import java.util.stream.Collectors;

import com.acd.researchrepo.dto.external.papers.PaginatedResponseDto;
import com.acd.researchrepo.dto.external.papers.ResearchPaperDto;
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

    public ResearchPaperService(
            ResearchPaperRepository researchPaperRepository,
            ResearchPaperMapper researchPaperMapper) {
        this.researchPaperRepository = researchPaperRepository;
        this.researchPaperMapper = researchPaperMapper;
    }

    public PaginatedResponseDto<ResearchPaperDto> getPapers(
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

        return PaginatedResponseDto.<ResearchPaperDto>builder()
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
}
