package com.acd.researchrepo.dto.external.filters;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class YearListResponse {
    private final List<Integer> years;
}
