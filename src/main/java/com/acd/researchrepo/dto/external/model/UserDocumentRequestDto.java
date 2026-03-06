package com.acd.researchrepo.dto.external.model;

import com.acd.researchrepo.model.RequestStatus;
import java.time.LocalDateTime;
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
