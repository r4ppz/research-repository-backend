package com.acd.researchrepo.dto.external.requests;

import java.time.LocalDateTime;

import com.acd.researchrepo.dto.external.model.ResearchPaperDto;
import com.acd.researchrepo.dto.external.model.UserDto;
import com.acd.researchrepo.model.RequestStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminRequestResponse {
    private Integer requestId;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDto user;
    private ResearchPaperDto paper;
}
