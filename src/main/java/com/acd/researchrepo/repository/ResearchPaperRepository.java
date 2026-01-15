package com.acd.researchrepo.repository;

import java.util.List;

import com.acd.researchrepo.model.ResearchPaper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResearchPaperRepository
        extends JpaRepository<ResearchPaper, Integer>, JpaSpecificationExecutor<ResearchPaper> {

    /**
     * Retrieves a list of distinct years in which research papers were submitted,
     * optionally filtered by department and active status.
     *
     * @param deptId     the department ID to filter by, or null for all departments
     * @param onlyActive if true, only include non-archived papers; if false,
     *                   include all
     * @return a list of distinct submission years in descending order
     */
    @Query("SELECT DISTINCT YEAR(p.submissionDate) FROM ResearchPaper p " +
            "WHERE (:deptId IS NULL OR p.department.departmentId = :deptId) " +
            "AND (:onlyActive = false OR p.archived = false) " +
            "ORDER BY YEAR(p.submissionDate) DESC")
    List<Integer> findDistinctYears(@Param("deptId") Integer deptId, @Param("onlyActive") boolean onlyActive);
}
