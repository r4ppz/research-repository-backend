package com.acd.researchrepo.dto.external.filters;

import java.util.List;

import com.acd.researchrepo.dto.external.model.DepartmentDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DepartmentListResponse {
    private List<DepartmentDto> departments;
}
