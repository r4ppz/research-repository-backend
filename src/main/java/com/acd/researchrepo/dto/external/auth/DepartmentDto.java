package com.acd.researchrepo.dto.external.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class DepartmentDto {
    private final Integer departmentId;
    private final String departmentName;
}
