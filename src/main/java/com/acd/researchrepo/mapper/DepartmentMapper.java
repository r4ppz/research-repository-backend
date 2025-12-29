package com.acd.researchrepo.mapper;

import com.acd.researchrepo.dto.external.model.DepartmentDto;
import com.acd.researchrepo.model.Department;

import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentDto toDto(Department department) {
        if (department == null) {
            return null;
        }

        return DepartmentDto.builder()
                .departmentId(department.getDepartmentId())
                .departmentName(department.getDepartmentName())
                .build();
    }

}
