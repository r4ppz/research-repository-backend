package com.acd.researchrepo.mapper;

import com.acd.researchrepo.dto.external.model.UserDto;
import com.acd.researchrepo.model.User;
import com.acd.researchrepo.model.UserRole;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final DepartmentMapper departmentMapper;

    public UserMapper(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto.UserDtoBuilder builder = UserDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .profilePictureUrl(user.getProfilePictureUrl());

        // If its a DEPARTMENT_ADMIN include its department, if its not then dont
        if (UserRole.DEPARTMENT_ADMIN.equals(user.getRole()) && user.getDepartment() != null) {
            builder.department(departmentMapper.toDto(user.getDepartment()));
        }

        return builder.build();
    }
}
