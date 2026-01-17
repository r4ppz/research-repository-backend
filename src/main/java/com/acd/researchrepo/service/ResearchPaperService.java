package com.acd.researchrepo.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.acd.researchrepo.util.SortUtil;

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
            List<Integer> departmentIds,
            List<Integer> years,
            Boolean archived,
            String sortBy,
            String sortOrder,
            int page,
            int size,
            CustomUserPrincipal userPrincipal) {

        if (RoleBasedAccess.isUserStudent(userPrincipal)) {
            archived = false;
        }

        // Sanitize sortBy and sortOrder against allowed fields using SortUtil
        Map<String, String> allowedFields = Map.of(
                "title", "title",
                "authorName", "authorName",
                "submissionDate", "submissionDate");

        Sort sort = SortUtil.createSort(sortBy, sortOrder, allowedFields, "submissionDate");

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<ResearchPaper> spec = ResearchPaperSpec
                .build(searchTerm, departmentIds, years, archived);

        Page<ResearchPaper> paperPage = researchPaperRepository.findAll(spec, pageable);

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
}
