package com.acd.researchrepo.dto.external.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class GoogleAuthRequest {
  @NotNull(message = "Auth code cannot be blank")
  private final String code;
}
