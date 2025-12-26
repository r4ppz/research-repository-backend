package com.acd.researchrepo.service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.acd.researchrepo.dto.external.auth.DepartmentDto;
import com.acd.researchrepo.mapper.DepartmentMapper;
import com.acd.researchrepo.model.Department;
import com.acd.researchrepo.model.enums.UserRole;
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

    public List<DepartmentDto> getAvailableDepartments(CustomUserPrincipal user) {
        UserRole role = user.getRole();

        List<Department> departments;

        if (role == UserRole.DEPARTMENT_ADMIN) {
            // Only their own department if assigned
            Integer deptId = user.getDepartmentId();
            if (deptId == null)
                return List.of();
            Department dep = departmentRepository.findById(deptId).orElse(null);

            if (dep == null)
                return List.of();
            departments = List.of(dep);

        } else {
            departments = departmentRepository.findAll();
        }

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
