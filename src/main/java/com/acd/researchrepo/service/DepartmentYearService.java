package com.acd.researchrepo.service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.acd.researchrepo.dto.external.auth.DepartmentDto;
import com.acd.researchrepo.mapper.DepartmentMapper;
import com.acd.researchrepo.model.Department;
import com.acd.researchrepo.model.ResearchPaper;
import com.acd.researchrepo.repository.DepartmentRepository;
import com.acd.researchrepo.repository.ResearchPaperRepository;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.util.RoleBasedAccess;

import org.springframework.stereotype.Service;

@Service
public class DepartmentYearService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final ResearchPaperRepository researchPaperRepository;

    public DepartmentYearService(
            DepartmentRepository departmentRepository,
            ResearchPaperRepository researchPaperRepository,
            DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.researchPaperRepository = researchPaperRepository;
        this.departmentMapper = departmentMapper;
    }

    public List<DepartmentDto> getAvailableDepartments(CustomUserPrincipal user) {
        List<Department> departments;

        departments = departmentRepository.findAll();

        // Only include departments that have at least one paper in scope
        Set<Integer> deptHasPaper = researchPaperRepository.findAll().stream()
                .map(p -> p.getDepartment().getDepartmentId())
                .collect(Collectors.toSet());

        List<DepartmentDto> departmentDto = departments
                .stream()
                .filter(deps -> deptHasPaper.contains(deps.getDepartmentId()))
                .sorted(Comparator.comparing(Department::getDepartmentName))
                .map(department -> departmentMapper.toDto(department))
                .collect(Collectors.toList());

        return departmentDto;
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
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }
}
