package com.acd.researchrepo.spec;

import java.util.List;

import com.acd.researchrepo.model.DocumentRequest;
import com.acd.researchrepo.model.RequestStatus;

import org.springframework.data.jpa.domain.Specification;

public class DocumentRequestSpec {

    public static Specification<DocumentRequest> hasDepartmentId(Integer departmentId) {
        if (departmentId == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction(); // Always true
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder
                .equal(root.get("paper").get("department").get("departmentId"), departmentId);
    }

    public static Specification<DocumentRequest> hasStatusIn(List<RequestStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction(); // Always true
        }
        return (root, query, criteriaBuilder) -> root.get("status").in(statuses);
    }

    public static Specification<DocumentRequest> hasSearchTerm(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction(); // Always true
        }

        String lowerCaseSearchTerm = "%" + searchTerm.toLowerCase().trim() + "%";
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("fullName")), lowerCaseSearchTerm),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("email")), lowerCaseSearchTerm),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("paper").get("title")), lowerCaseSearchTerm));
    }

    public static Specification<DocumentRequest> adminRequestFilter(
            Integer departmentId, List<RequestStatus> statuses, String searchTerm) {
        return Specification.where(hasDepartmentId(departmentId))
                .and(hasStatusIn(statuses))
                .and(hasSearchTerm(searchTerm));
    }

    public static Specification<DocumentRequest> adminRequestFilter(
            Integer departmentId, List<RequestStatus> statuses) {
        return adminRequestFilter(departmentId, statuses, null);
    }
}
