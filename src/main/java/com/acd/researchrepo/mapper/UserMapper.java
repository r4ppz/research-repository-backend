package com.acd.researchrepo.mapper;

import com.acd.researchrepo.dto.DepartmentDto;
import com.acd.researchrepo.dto.UserDto;
import com.acd.researchrepo.model.Department;
import com.acd.researchrepo.model.User;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .department(toDepartmentDto(user.getDepartment()))
                .build();
    }

    public UserDto toDtoWithoutDepartment(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .department(null)
                .build();
    }

    private DepartmentDto toDepartmentDto(Department department) {
        if (department == null) {
            return null;
        }

        return DepartmentDto.builder()
                .departmentId(department.getDepartmentId())
                .departmentName(department.getDepartmentName())
                .build();
    }
}
