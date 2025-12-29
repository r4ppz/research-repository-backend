package com.acd.researchrepo.dto.external.papers;

import java.time.LocalDateTime;

import com.acd.researchrepo.model.RequestStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaperUserRequestResponse {
    private Integer requestId;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
