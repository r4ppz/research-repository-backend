package com.acd.researchrepo.specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.acd.researchrepo.model.ResearchPaper;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

public class ResearchPaperSpecification {

    /**
     * Builds a dynamic JPA Specification for filtering ResearchPaper entities.
     *
     * @param searchTerm    Search string to match against title, authorName, or
     *                      abstractText (case-insensitive).
     * @param departmentIds Comma-separated department IDs to filter by department.
     * @param years         Comma-separated years to filter by submissionDate.
     * @param archived      Boolean flag to filter by archived status.
     * @return Specification for querying ResearchPaper entities with the given
     *         filters.
     */
    public static Specification<ResearchPaper> build(
            String searchTerm,
            String departmentIds,
            String years,
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

            // Department filtering - parse comma-separated string into list of integers
            if (departmentIds != null && !departmentIds.isEmpty()) {
                String[] deptIds = departmentIds.split(",");
                List<Integer> deptIdList = new ArrayList<>();
                for (String id : deptIds) {
                    try {
                        deptIdList.add(Integer.parseInt(id.trim()));
                    } catch (NumberFormatException e) {
                        // Skip invalid department IDs
                    }
                }
                if (!deptIdList.isEmpty()) {
                    predicates.add(root.get("department").get("departmentId").in(deptIdList));
                }
            }

            // Year filtering - parse comma-separated string into list of integers and
            // create OR condition
            if (years != null && !years.isEmpty()) {
                String[] yearArray = years.split(",");
                List<Integer> yearList = new ArrayList<>();
                for (String yearStr : yearArray) {
                    try {
                        yearList.add(Integer.parseInt(yearStr.trim()));
                    } catch (NumberFormatException e) {
                        // Skip invalid years
                    }
                }
                if (!yearList.isEmpty()) {
                    // Create OR condition for all years: (year1 OR year2 OR year3 ...)
                    List<Predicate> yearPredicates = new ArrayList<>();
                    for (Integer year : yearList) {
                        yearPredicates.add(cb.between(
                                root.get("submissionDate"),
                                LocalDate.of(year, 1, 1),
                                LocalDate.of(year, 12, 31)));
                    }
                    predicates.add(cb.or(yearPredicates.toArray(new Predicate[0])));
                }
            }

            // Archive filtering
            if (archived != null) {
                predicates.add(cb.equal(root.get("archived"), archived));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
