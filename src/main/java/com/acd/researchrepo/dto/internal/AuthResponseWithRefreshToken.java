package com.acd.researchrepo.dto.internal;

import com.acd.researchrepo.dto.external.UserDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
