package com.acd.researchrepo.dto.internal;

import com.acd.researchrepo.dto.external.auth.UserDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthTokenContainer {
    private final String accessToken;
    private final String refreshToken;
    private final UserDto user;
}
