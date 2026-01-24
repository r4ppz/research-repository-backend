package com.acd.researchrepo.dto.external.model;

import com.acd.researchrepo.model.UserRole;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UserDto {
    private final Integer userId;
    private final String email;
    private final String fullName;
    private final UserRole role;
    private final DepartmentDto department;
    private final String profilePictureUrl;
}
