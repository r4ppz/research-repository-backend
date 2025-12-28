package com.acd.researchrepo.dto.external.requests;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDocumentRequestsResponse {
    private List<UserDocumentRequestDto> requests;
}