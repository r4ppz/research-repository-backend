package com.acd.researchrepo.dto.external.auth;

import com.acd.researchrepo.dto.external.model.UserDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private final String accessToken;
    private final UserDto user;
}
