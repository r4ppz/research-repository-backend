package com.acd.researchrepo.dto.external.requests;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.Size;

@Getter
@Jacksonized
@Builder
public class RejectRequestRequest {
    @Size(max = 255, message = "Reason must be at most 255 characters")
    private String reason;
}
