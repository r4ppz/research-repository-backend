package com.acd.researchrepo.dto.external.requests;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@Builder
public class CreateRequestRequest {
    private Integer paperId;
}
