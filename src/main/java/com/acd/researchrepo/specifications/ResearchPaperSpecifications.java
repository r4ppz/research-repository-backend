package com.acd.researchrepo.specifications;

import java.util.ArrayList;
import java.util.List;

import com.acd.researchrepo.model.ResearchPaper;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

public class ResearchPaperSpecifications {
    public static Specification<ResearchPaper> build(
            String searchTerm,
            List<Integer> departmentIds,
            Integer year,
            Boolean archived) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search string (title, authorName, abstractText)
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String like = "%" + searchTerm.toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("title")), like),
                                cb.like(cb.lower(root.get("authorName")), like),
                                cb.like(cb.lower(root.get("abstractText")), like)

                        ));
            }

            // Department filtering
            if (departmentIds != null && !departmentIds.isEmpty()) {
                predicates.add(root.get("department").get("departmentId").in(departmentIds));
            }

            // Year matching (can use extract function; works with Hibernate/Postgres)
            if (year != null) {
                predicates.add(cb.equal(cb.function("year", Integer.class, root.get("submissionDate")), year));
            }

            // Archive filtering
            if (archived != null) {
                predicates.add(cb.equal(root.get("archived"), archived));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
