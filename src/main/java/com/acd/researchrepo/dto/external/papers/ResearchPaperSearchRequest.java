package com.acd.researchrepo.dto.external.papers;

import java.util.List;

import com.acd.researchrepo.util.enums.ResearchPaperSortField;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

@Data
public class ResearchPaperSearchRequest {

    private String search;
    private List<Integer> departmentId;
    private List<@Min(value = 1900, message = "Year must be at least 1900") @Max(value = 2100, message = "Year cannot exceed 2100") Integer> year;
    private Boolean archived;

    @Min(value = 0, message = "Page number cannot be negative")
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private int size = 20;

    @Pattern(regexp = "submissionDate|title|authorName", message = "Invalid sort field. Must be: submissionDate, title, authorName")
    private String sortBy = "submissionDate";

    @Pattern(regexp = "(?i)asc|desc", message = "Invalid sort order. Must be: asc, desc")
    private String sortOrder = "desc";

    public Pageable toPageable() {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String mappedField = ResearchPaperSortField.fromApiField(sortBy, "submissionDate");
        return PageRequest.of(page, size, Sort.by(direction, mappedField));
    }
}
