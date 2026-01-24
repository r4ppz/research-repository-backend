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
            Integer departmentId,
            List<RequestStatus> statuses,
            String searchTerm) {
        Specification<DocumentRequest> spec = (root, query, cb) -> cb.conjunction();
        spec = spec.and(hasDepartmentId(departmentId))
                .and(hasStatusIn(statuses))
                .and(hasSearchTerm(searchTerm));
        return spec;
    }

    public static Specification<DocumentRequest> adminRequestFilter(
            Integer departmentId, List<RequestStatus> statuses) {
        return adminRequestFilter(departmentId, statuses, null);
    }

    public static Specification<DocumentRequest> hasUserId(Integer userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("userId"), userId);
    }

    public static Specification<DocumentRequest> paperNotArchived() {
        return (root, query, cb) -> cb.isFalse(root.get("paper").get("archived"));
    }

    public static Specification<DocumentRequest> userRequestFilter(
            Integer userId,
            List<RequestStatus> statuses,
            String search) {

        Specification<DocumentRequest> spec = (root, query, cb) -> cb.conjunction();
        spec = spec.and(hasUserId(userId))
                .and(hasStatusIn(statuses))
                .and(hasSearchTerm(search))
                .and(paperNotArchived());
        return spec;
    }
}
