package com.acd.researchrepo.spec;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.acd.researchrepo.model.ResearchPaper;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

public class ResearchPaperSpec {

    /**
     * Builds a JPA Specification for filtering ResearchPaper entities based on
     * search term,
     * department IDs, years, and archived status.
     *
     * @param searchTerm    Text to search in title, author, or abstract
     *                      (case-insensitive).
     * @param departmentIds List of department IDs to filter by.
     * @param years         List of years to filter submission dates.
     * @param archived      Archived status to filter by.
     * @return Specification for querying ResearchPaper entities.
     */
    public static Specification<ResearchPaper> build(
            String searchTerm,
            List<Integer> departmentIds,
            List<Integer> years,
            Boolean archived) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Search (Title, Author, Abstract)
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String like = "%" + searchTerm.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("authorName")), like),
                        cb.like(cb.lower(root.get("abstractText")), like)));
            }

            // Department filtering
            if (departmentIds != null && !departmentIds.isEmpty()) {
                predicates.add(root.get("department").get("departmentId").in(departmentIds));
            }

            // Year filtering
            if (years != null && !years.isEmpty()) {
                List<Predicate> yearPredicates = new ArrayList<>();
                for (Integer year : years) {
                    yearPredicates.add(cb.between(
                            root.get("submissionDate"),
                            LocalDate.of(year, 1, 1),
                            LocalDate.of(year, 12, 31)));
                }
                predicates.add(cb.or(yearPredicates.toArray(new Predicate[0])));
            }

            // Archive filtering
            if (archived != null) {
                predicates.add(cb.equal(root.get("archived"), archived));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
