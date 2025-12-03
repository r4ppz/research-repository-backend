package com.acd.researchrepo.controller;

import com.acd.researchrepo.dto.AuthResponse;
import com.acd.researchrepo.dto.GoogleAuthRequest;
import com.acd.researchrepo.service.AuthService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${app.refresh-token. cookie-name:refreshToken}")
    private String refreshTokenCookieName;

    @Value("${app.refresh-token.max-age:2592000}")
    private int refreshTokenMaxAge;

    @Value("${app.environment:development}")
    private String environment;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/google
     * Authenticate user with Google OAuth authorization code
     *
     * Request body: { "code": "authorization_code_from_google" }
     * Response: { "accessToken": "jwt_token", "user": {... } }
     * Sets refresh token as HttpOnly cookie
     */
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> authenticateWithGoogle(
            @Valid @RequestBody GoogleAuthRequest request,
            HttpServletResponse response) {

        try {
            var authResult = authService
                    .authenticateWithGoogleAndGetRefreshToken(request.getCode());
            setRefreshTokenCookie(response, authResult.getRefreshToken());
            AuthResponse publicResponse = AuthResponse.builder()
                    .accessToken(authResult.getAccessToken())
                    .user(authResult.getUser())
                    .build();

            return ResponseEntity.ok(publicResponse);

        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Helper method to set refresh token as HttpOnly cookie
     * Cookie settings change based on environment (dev vs prod)
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(refreshTokenCookieName, refreshToken);

        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth/");
        cookie.setMaxAge(refreshTokenMaxAge);

        if ("production".equalsIgnoreCase(environment)) {
            cookie.setSecure(true);
            cookie.setAttribute("SameSite", "Strict");
        } else {
            cookie.setSecure(false);
            cookie.setAttribute("SameSite", "Lax");
        }

        response.addCookie(cookie);
    }
}
