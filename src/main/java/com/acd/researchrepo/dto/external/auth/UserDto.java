package com.acd.researchrepo.dto.external.auth;

import com.acd.researchrepo.model.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Integer userId;
    private String email;
    private String fullName;
    private UserRole role;
    private DepartmentDto department;
}
