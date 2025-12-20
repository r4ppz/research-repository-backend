package com.acd.researchrepo.dto.internal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshResult {
    private final String accessToken;
    private final String refreshToken;
}
