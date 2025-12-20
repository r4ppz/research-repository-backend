package com.acd.researchrepo.controller;

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
import com.acd.researchrepo.util.CookieUtil;

import org.springframework.http.ResponseEntity;
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
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final CookieUtil cookieUtil;

    public AuthController(
            AuthService authService,
            JwtService jwtService,
            UserMapper userMapper,
            UserRepository userRepository,
            CookieUtil cookieUtil) {
        this.jwtService = jwtService;
        this.authService = authService;
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.cookieUtil = cookieUtil;
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

        cookieUtil.setRefreshTokenCookie(response, authContiner.getRefreshToken());
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
            throw new UnauthorizedException("Refresh token not found in cookies");
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
            throw new UnauthorizedException("Invalid refresh token", e);
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
}
