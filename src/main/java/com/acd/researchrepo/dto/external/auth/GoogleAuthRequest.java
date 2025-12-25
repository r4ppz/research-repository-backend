package com.acd.researchrepo.dto.external.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotNull;

@Getter
@Builder
@Jacksonized
public class GoogleAuthRequest {
    @NotNull(message = "Auth code cannot be blank")
    private final String code;
}
