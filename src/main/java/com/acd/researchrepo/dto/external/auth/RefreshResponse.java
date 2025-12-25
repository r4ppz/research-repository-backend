package com.acd.researchrepo.dto.external.auth;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RefreshResponse {
    private final String accessToken;
}
