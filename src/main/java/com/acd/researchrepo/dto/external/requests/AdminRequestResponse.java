package com.acd.researchrepo.dto.external.requests;

import java.time.LocalDateTime;

import com.acd.researchrepo.dto.external.model.ResearchPaperDto;
import com.acd.researchrepo.model.RequestStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminRequestResponse {
    private String id;
    private String departmentId;
    private String userId;
    private ResearchPaperDto paper;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
