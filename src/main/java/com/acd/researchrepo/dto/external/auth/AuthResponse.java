package com.acd.researchrepo.dto.external.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private final String accessToken;
    private final UserDto user;
}
