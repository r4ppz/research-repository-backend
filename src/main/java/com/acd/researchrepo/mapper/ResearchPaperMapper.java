package com.acd.researchrepo.mapper;

import com.acd.researchrepo.dto.external.papers.ResearchPaperDto;
import com.acd.researchrepo.model.ResearchPaper;

import org.springframework.stereotype.Component;

@Component
public class ResearchPaperMapper {

    private final DepartmentMapper departmentMapper;

    public ResearchPaperMapper(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    public ResearchPaperDto toDto(ResearchPaper paper) {
        if (paper == null) {
            return null;
        }

        return ResearchPaperDto.builder()
                .paperId(paper.getPaperId())
                .title(paper.getTitle())
                .authorName(paper.getAuthorName())
                .abstractText(paper.getAbstractText())
                .department(departmentMapper.toDto(paper.getDepartment()))
                .submissionDate(paper.getSubmissionDate())
                .filePath(paper.getFilePath())
                .archived(paper.getArchived())
                .archivedAt(paper
                        .getArchivedAt())
                .build();
    }
}
