package com.acd.researchrepo.dto.external.requests;

import java.util.List;

import com.acd.researchrepo.model.RequestStatus;
import com.acd.researchrepo.util.enums.DocumentRequestSortField;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

@Data
public class DocumentRequestSearchRequest {

    private String search;
    private List<RequestStatus> status;
    private Integer departmentId;

    @Min(value = 0, message = "Page number cannot be negative")
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private int size = 20;

    @Pattern(regexp = "createdAt|status|paper.title|user.fullName", message = "Invalid sort field. Must be: createdAt, status, paper.title, user.fullName")
    private String sortBy = "createdAt";

    @Pattern(regexp = "(?i)asc|desc", message = "Invalid sort order. Must be: asc, desc")
    private String sortOrder = "desc";

    public Pageable toPageable() {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String mappedField = DocumentRequestSortField.fromApiField(sortBy, "createdAt");
        return PageRequest.of(page, size, Sort.by(direction, mappedField));
    }
}
