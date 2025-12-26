package com.acd.researchrepo.dto.external.papers;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaginatedResponseDto<T> {
    private List<T> content;
    private int totalElements;
    private int totalPages;
    private int number; // Current page number (0-indexed)
    private int size; // Page size
}
