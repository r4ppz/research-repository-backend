package com.acd.researchrepo.dto.external.filters;

import com.acd.researchrepo.dto.external.model.DepartmentDto;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DepartmentListResponse {
  private List<DepartmentDto> departments;
}
