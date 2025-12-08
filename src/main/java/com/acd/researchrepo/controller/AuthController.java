package com.acd.researchrepo.controller;

import java.util.Map;

import com.acd.researchrepo.dto.external.auth.AuthResponse;
import com.acd.researchrepo.dto.external.auth.GoogleAuthRequest;
import com.acd.researchrepo.dto.external.auth.RefreshResponse;
import com.acd.researchrepo.service.AuthService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Value("${app.refresh-token.cookie-name:refreshToken}")
    private String refreshTokenCookieName;

    @Value("${app.refresh-token.max-age:2592000}")
    private int refreshTokenMaxAge;

    @Value("${app.environment:development}")
    private String environment;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> authenticateWithGoogle(
            @Valid @RequestBody GoogleAuthRequest request,
            HttpServletResponse response) {
        log.info("Request code is recieved! (I think)");

        var authResult = authService
                .authenticateWithGoogle(request.getCode());
        log.info("Auth service is success yay!");

        setRefreshTokenCookie(response, authResult.getRefreshToken());
        log.info("Refresh token has been set!");

        var publicResponse = AuthResponse
                .builder()
                .accessToken(authResult.getAccessToken())
                .user(authResult.getUser())
                .build();

        log.info("Login success :)  returning the public response which is accessToken and user");
        return ResponseEntity.ok(publicResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refreshAccessToken(HttpServletRequest request,
            HttpServletResponse response) {

        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            log.error("Refresh token is nall bruh");
            return ResponseEntity.status(401).body(new RefreshResponse(null));
        }

        try {
            var refreshResult = authService.refreshAccessToken(refreshToken);
            setRefreshTokenCookie(response, refreshResult.getRefreshToken());
            var refreshResponse = new RefreshResponse(refreshResult.getAccessToken());
            return ResponseEntity.ok(refreshResponse);
        } catch (RuntimeException e) {
            clearRefreshTokenCookie(response);
            return ResponseEntity.status(401).body(new RefreshResponse(null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            authService.revokeRefreshToken(refreshToken);
        }
        clearRefreshTokenCookie(response);
        return ResponseEntity.ok().body(Map.of("message", "Logged out successfully"));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(refreshTokenCookieName, refreshToken);

        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(refreshTokenMaxAge);

        if ("production".equalsIgnoreCase(environment)) {
            cookie.setSecure(true);
            cookie.setAttribute("SameSite", "None");
        } else {
            cookie.setSecure(false);
            cookie.setAttribute("SameSite", "Lax");
        }

        response.addCookie(cookie);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null)
            return null;
        for (Cookie cookie : request.getCookies()) {
            if (refreshTokenCookieName.equals(cookie.getName())) {
                System.out.println("Cookie: " + cookie.getName() + "=" + cookie.getValue());
                return cookie.getValue();
            }

        }
        System.out.println("No cookies sent with request");
        return null;

    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(refreshTokenCookieName, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        if ("production".equalsIgnoreCase(environment)) {
            cookie.setSecure(true);
            cookie.setAttribute("SameSite", "None");
        } else {
            cookie.setSecure(false);
            cookie.setAttribute("SameSite", "Lax");
        }
        response.addCookie(cookie);
    }
}
