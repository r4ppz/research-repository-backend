package com.acd.researchrepo.dto.external.papers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.acd.researchrepo.dto.external.auth.DepartmentDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResearchPaperDto {
    private Integer paperId;
    private String title;
    private String authorName;
    private String abstractText;
    private DepartmentDto department;
    private LocalDate submissionDate;
    private String filePath;
    private Boolean archived;
    private LocalDateTime archivedAt;
}
