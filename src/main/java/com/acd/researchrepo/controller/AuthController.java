package com.acd.researchrepo.controller;

import java.util.Map;
import java.util.Optional;

import com.acd.researchrepo.dto.external.auth.AuthResponse;
import com.acd.researchrepo.dto.external.auth.GoogleAuthRequest;
import com.acd.researchrepo.dto.external.auth.RefreshResponse;
import com.acd.researchrepo.dto.external.auth.UserDto;
import com.acd.researchrepo.exception.InvalidTokenException;
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

    @Value("${app.environment:development}")
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

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // TODO: maybe add custom exception here ?
            log.error("No auth header found!! WHAT!!!");
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);
        try {
            Integer userId = jwtService.getUserIdFromToken(token);
            Optional<User> user = userRepository.findById(userId);
            if (!user.isPresent()) {
                return ResponseEntity.status(404).build();
            }

            UserDto userDto = userMapper.toDto(user.get());
            return ResponseEntity.ok(userDto);
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(401).build();
        }
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
        if (request.getCookies() == null) {
            // TODO: maybe add custom exeption here no?
            log.error("Yea refresh token is null buddy");
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (refreshTokenCookieName.equals(cookie.getName())) {
                log.info("Cookie: {} = {}", cookie.getValue(), cookie.getName());
                return cookie.getValue();
            }

        }
        log.info("No cookies sent with request :( ");
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
