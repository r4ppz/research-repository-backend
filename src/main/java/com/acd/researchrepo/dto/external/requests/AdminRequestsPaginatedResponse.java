package com.acd.researchrepo.dto.external.requests;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminRequestsPaginatedResponse {
    private List<AdminRequestDetailResponse> content;
    private int totalElements;
    private int totalPages;
    private int number;
    private int size;
}
