package com.acd.researchrepo.service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.acd.researchrepo.dto.external.model.DepartmentDto;
import com.acd.researchrepo.mapper.DepartmentMapper;
import com.acd.researchrepo.model.Department;
import com.acd.researchrepo.repository.DepartmentRepository;
import com.acd.researchrepo.repository.ResearchPaperRepository;
import com.acd.researchrepo.security.CustomUserPrincipal;

import org.springframework.stereotype.Service;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final ResearchPaperRepository researchPaperRepository;

    public DepartmentService(
            DepartmentRepository departmentRepository,
            ResearchPaperRepository researchPaperRepository,
            DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.researchPaperRepository = researchPaperRepository;
        this.departmentMapper = departmentMapper;
    }

    /**
     * Retrieves a list of departments that have at least one associated research
     * paper.
     *
     * @param user the authenticated user requesting the departments
     * @return a list of DepartmentDto objects
     */
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
}
