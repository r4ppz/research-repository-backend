package com.acd.researchrepo.controller;

import java.util.Map;
import java.util.Optional;

import com.acd.researchrepo.dto.external.auth.AuthResponse;
import com.acd.researchrepo.dto.external.auth.GoogleAuthRequest;
import com.acd.researchrepo.dto.external.auth.RefreshResponse;
import com.acd.researchrepo.dto.external.auth.UserDto;
import com.acd.researchrepo.dto.internal.AuthTokenContainer;
import com.acd.researchrepo.dto.internal.RefreshResult;
import com.acd.researchrepo.exception.InvalidTokenException;
import com.acd.researchrepo.exception.NotFoundException;
import com.acd.researchrepo.exception.UnauthorizedException;
import com.acd.researchrepo.mapper.UserMapper;
import com.acd.researchrepo.model.User;
import com.acd.researchrepo.repository.UserRepository;
import com.acd.researchrepo.service.AuthService;
import com.acd.researchrepo.service.JwtService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Value("${app.refresh-token.cookie-name:refreshToken}")
    private String refreshTokenCookieName;

    @Value("${app.refresh-token.max-age:2592000}")
    private int refreshTokenMaxAge;

    @Value("${spring.profiles.active}")
    private String environment;

    public AuthController(
            AuthService authService,
            JwtService jwtService,
            UserMapper userMapper,
            UserRepository userRepository) {
        this.jwtService = jwtService;
        this.authService = authService;
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(
            @Valid @RequestBody GoogleAuthRequest request,
            HttpServletResponse response) {
        log.debug("api/auth/google endpoint hit!!");

        AuthTokenContainer authContiner = authService.authenticateWithGoogle(request.getCode());
        log.debug("Google authentication completed successfully.");

        AuthResponse authResponse = AuthResponse
                .builder()
                .accessToken(authContiner.getAccessToken())
                .user(authContiner.getUser())
                .build();
        log.debug("AuthResponse built with user and access token details.");

        setRefreshTokenCookie(response, authContiner.getRefreshToken());
        log.debug("Refresh token cookie set in response.");

        log.debug("Returning authentication response.");
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refreshAccessToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        log.debug("api/auth/refresh endpoint hit!!");

        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            throw new UnauthorizedException("Refresh token not found in cookies");
        }

        try {
            RefreshResult refreshResult = authService.refreshAccessToken(refreshToken);
            setRefreshTokenCookie(response, refreshResult.getRefreshToken());
            RefreshResponse refreshResponse = new RefreshResponse(refreshResult.getAccessToken());
            return ResponseEntity.ok(refreshResponse);
        } catch (RuntimeException e) {
            clearRefreshTokenCookie(response);
            throw new UnauthorizedException("Invalid refresh token", e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        log.debug("api/auth/logout endpoint hit!!");

        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            authService.revokeRefreshToken(refreshToken);
        }
        clearRefreshTokenCookie(response);
        return ResponseEntity.ok().body(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(HttpServletRequest request) {
        log.debug("api/auth/me endpoint hit!!");

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        try {
            Integer userId = jwtService.getUserIdFromToken(token);
            Optional<User> user = userRepository.findById(userId);
            if (!user.isPresent()) {
                throw new NotFoundException("User not found");
            }

            UserDto userDto = userMapper.toDto(user.get());
            return ResponseEntity.ok(userDto);
        } catch (InvalidTokenException e) {
            throw new UnauthorizedException("Invalid access token");
        }
    }

    private void setRefreshTokenCookie(
            HttpServletResponse response,
            String refreshToken) {
        Cookie cookie = new Cookie(refreshTokenCookieName, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(refreshTokenMaxAge);
        if ("prod".equalsIgnoreCase(environment)) {
            cookie.setSecure(true);
            cookie.setAttribute("SameSite", "None");
        } else {
            cookie.setSecure(false);
            cookie.setAttribute("SameSite", "Lax");
        }
        response.addCookie(cookie);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            log.error("Yea refresh token is null buddy");
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (refreshTokenCookieName.equals(cookie.getName())) {
                log.debug("Cookie: {} = {}", cookie.getName(), cookie.getValue());
                return cookie.getValue();
            }

        }
        log.debug("No cookies sent with request !");
        return null;
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(refreshTokenCookieName, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        if ("prod".equalsIgnoreCase(environment)) {
            cookie.setSecure(true);
            cookie.setAttribute("SameSite", "None");
        } else {
            cookie.setSecure(false);
            cookie.setAttribute("SameSite", "Lax");
        }
        response.addCookie(cookie);
    }
}
