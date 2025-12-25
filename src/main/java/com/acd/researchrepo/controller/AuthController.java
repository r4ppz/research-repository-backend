package com.acd.researchrepo.controller;

import com.acd.researchrepo.dto.external.auth.AuthResponse;
import com.acd.researchrepo.dto.external.auth.GoogleAuthRequest;
import com.acd.researchrepo.dto.external.auth.RefreshResponse;
import com.acd.researchrepo.dto.external.auth.UserDto;
import com.acd.researchrepo.dto.internal.AuthTokenContainer;
import com.acd.researchrepo.dto.internal.RefreshResult;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.mapper.UserMapper;
import com.acd.researchrepo.repository.UserRepository;
import com.acd.researchrepo.security.CustomUserPrincipal;
import com.acd.researchrepo.service.AuthService;
import com.acd.researchrepo.service.JwtService;
import com.acd.researchrepo.util.CookieUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final UserMapper userMapper;
    private final CookieUtil cookieUtil;

    public AuthController(
            AuthService authService,
            JwtService jwtService,
            UserMapper userMapper,
            UserRepository userRepository,
            CookieUtil cookieUtil) {
        this.authService = authService;
        this.userMapper = userMapper;
        this.cookieUtil = cookieUtil;
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(
            @Valid @RequestBody GoogleAuthRequest request,
            HttpServletResponse response) {
        log.debug("api/auth/google endpoint hit!!");

        AuthTokenContainer authContainer = authService.authenticateWithGoogle(request.getCode());
        log.debug("Google authentication completed successfully.");

        AuthResponse authResponse = AuthResponse
                .builder()
                .accessToken(authContainer.getAccessToken())
                .user(authContainer.getUser())
                .build();
        log.debug("AuthResponse built with user and access token details.");

        cookieUtil.setRefreshTokenCookie(response, authContainer.getRefreshToken());
        log.debug("Refresh token cookie set in response.");

        log.debug("Returning authentication response.");
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
            RefreshResult refreshResult = authService.refreshAccessToken(refreshToken);
            cookieUtil.setRefreshTokenCookie(response, refreshResult.getRefreshToken());

            RefreshResponse refreshResponse = RefreshResponse
                    .builder()
                    .accessToken(refreshResult.getAccessToken())
                    .build();

            return ResponseEntity.ok(refreshResponse);
        } catch (RuntimeException e) {
            cookieUtil.clearRefreshTokenCookie(response);
            throw new ApiException(ErrorCode.REFRESH_TOKEN_REVOKED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
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

    // This differs from the docs (/users/me).
    // Ill update the documentation or change this idk.
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal CustomUserPrincipal principal) {
        log.debug("api/auth/me endpoint hit!!");

        if (principal == null) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED);
        }

        UserDto userDto = userMapper.toDto(principal.getUser());
        return ResponseEntity.ok(userDto);
    }
}
