package com.acd.researchrepo.mapper;

import com.acd.researchrepo.dto.DepartmentDto;
import com.acd.researchrepo.dto.UserDto;
import com.acd.researchrepo.model.Department;
import com.acd.researchrepo.model.User;
import com.acd.researchrepo.model.enums.UserRole;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        // NOTE: I know the way I wrote this is... ahh
        boolean haveDepartment = user.getRole().equals(UserRole.DEPARTMENT_ADMIN);
        if (haveDepartment) {
            return UserDto.builder()
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .department(toDepartmentDto(user.getDepartment()))
                    .build();
        } else {
            return UserDto.builder()
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .department(null)
                    .build();

        }
    }

    // NOTE: maybe add this to its on file
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
