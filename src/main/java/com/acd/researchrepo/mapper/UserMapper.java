package com.acd.researchrepo.mapper;

import com.acd.researchrepo.dto.external.auth.UserDto;
import com.acd.researchrepo.model.User;
import com.acd.researchrepo.model.enums.UserRole;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    DepartmentMapper departmentMapper;

    public UserMapper(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

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
                    .department(departmentMapper.toDto(user.getDepartment()))
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
}
