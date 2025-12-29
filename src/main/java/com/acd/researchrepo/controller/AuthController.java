package com.acd.researchrepo.controller;

import com.acd.researchrepo.dto.external.auth.AuthResponse;
import com.acd.researchrepo.dto.external.auth.GoogleAuthRequest;
import com.acd.researchrepo.dto.external.auth.RefreshResponse;
import com.acd.researchrepo.dto.internal.AuthTokenContainer;
import com.acd.researchrepo.dto.internal.RefreshResult;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.service.AuthService;
import com.acd.researchrepo.util.CookieUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    public AuthController(AuthService authService, CookieUtil cookieUtil) {
        this.authService = authService;
        this.cookieUtil = cookieUtil;
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(
            @Valid @RequestBody GoogleAuthRequest request,
            HttpServletResponse response) {
        log.debug("api/auth/google endpoint hit!!");

        AuthTokenContainer tokens = authService.authenticateWithGoogle(request.getCode());
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(tokens.getAccessToken())
                .user(tokens.getUser())
                .build();

        cookieUtil.setRefreshTokenCookie(response, tokens.getRefreshToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refreshAccessToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        log.debug("api/auth/refresh endpoint hit!!");

        String refreshToken = cookieUtil.extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            throw new ApiException(ErrorCode.REFRESH_TOKEN_REVOKED);
        }

        try {
            RefreshResult result = authService.refreshAccessToken(refreshToken);
            cookieUtil.setRefreshTokenCookie(response, result.getRefreshToken());

            RefreshResponse refreshResponse = RefreshResponse.builder()
                    .accessToken(result.getAccessToken())
                    .build();

            return ResponseEntity.ok(refreshResponse);
        } catch (RuntimeException e) {
            cookieUtil.clearRefreshTokenCookie(response);
            throw new ApiException(ErrorCode.REFRESH_TOKEN_REVOKED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        log.debug("api/auth/logout endpoint hit!!");

        String refreshToken = cookieUtil.extractRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            authService.revokeRefreshToken(refreshToken);
        }
        cookieUtil.clearRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }
}
