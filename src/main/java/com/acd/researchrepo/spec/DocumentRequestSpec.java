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

    public static Specification<DocumentRequest> adminRequestFilter(
            Integer departmentId, List<RequestStatus> statuses) {
        return Specification.where(hasDepartmentId(departmentId))
                .and(hasStatusIn(statuses));
    }
}
