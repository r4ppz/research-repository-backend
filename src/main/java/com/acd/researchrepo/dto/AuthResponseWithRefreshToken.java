package com.acd.researchrepo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Internal service return type. NOT part of the public API.
 * Contains the access token (for JSON response) and the refresh token
 * (so controller can set it as HttpOnly cookie).
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseWithRefreshToken {
    private String accessToken;
    private String refreshToken;
    private UserDto user;
}
