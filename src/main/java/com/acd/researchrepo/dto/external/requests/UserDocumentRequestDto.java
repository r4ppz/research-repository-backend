package com.acd.researchrepo.dto.external.requests;

import java.time.LocalDateTime;

import com.acd.researchrepo.dto.external.papers.ResearchPaperDto;
import com.acd.researchrepo.model.RequestStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDocumentRequestDto {
    private Integer requestId;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ResearchPaperDto paper;
}
