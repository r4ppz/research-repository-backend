package com.acd.researchrepo.dto.external.papers;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaginatedResponse<T> {
    private List<T> content;
    private int totalElements;
    private int totalPages;
    private int number; // Current page number (0-indexed)
    private int size; // Page size

    public static <Entity, Dto> PaginatedResponse<Dto> fromPage(
            Page<Entity> page,
            Function<Entity, Dto> mapper) {
        return PaginatedResponse.<Dto>builder()
                .content(page.getContent().stream().map(mapper).collect(Collectors.toList()))
                .totalElements((int) page.getTotalElements())
                .totalPages(page.getTotalPages())
                .number(page.getNumber())
                .size(page.getSize())
                .build();
    }
}
