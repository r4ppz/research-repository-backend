package com.acd.researchrepo.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.acd.researchrepo.dto.internal.AuthResponseWithRefreshToken;
import com.acd.researchrepo.dto.internal.GoogleUserInfo;
import com.acd.researchrepo.dto.internal.RefreshResult;
import com.acd.researchrepo.mapper.UserMapper;
import com.acd.researchrepo.model.RefreshToken;
import com.acd.researchrepo.model.User;
import com.acd.researchrepo.model.enums.UserRole;
import com.acd.researchrepo.repository.RefreshTokenRepository;
import com.acd.researchrepo.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService {

    @Value("${app.refresh-token.max-age:2592000}")
    private int refreshTokenMaxAge;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final GoogleAuthService googleAuthService;
    private final UserMapper userMapper;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            GoogleAuthService googleAuthService,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.googleAuthService = googleAuthService;
        this.userMapper = userMapper;
    }

    @Transactional
    public AuthResponseWithRefreshToken authenticateWithGoogle(String googleAuthCode) {
        GoogleUserInfo googleUserInfo = googleAuthService.validateCodeAndGetUserInfo(googleAuthCode);
        log.info("Successfully exchange auth code for token yay!");

        User user = findOrCreateUser(googleUserInfo);
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        return AuthResponseWithRefreshToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(userMapper.toDto(user))
                .build();
    }

    @Transactional
    public RefreshResult refreshAccessToken(String refreshTokenValue) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (oldToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(oldToken);
            throw new RuntimeException("Refresh token expired");
        }

        User user = oldToken.getUser();
        // Mark the old token as used/invalidate it to prevent reuse
        refreshTokenRepository.delete(oldToken);

        // Create a new refresh token for future use
        RefreshToken newToken = createRefreshToken(user);
        String newAccessToken = jwtService.generateAccessToken(user);
        return new RefreshResult(newAccessToken, newToken.getToken());
    }

    private User findOrCreateUser(GoogleUserInfo googleUserInfo) {
        Optional<User> existingUser = userRepository.findByEmail(googleUserInfo.getEmail());

        if (existingUser.isPresent()) {
            log.info("User already exist!");
            return existingUser.get();
        }

        User newUser = User.builder()
                .email(googleUserInfo.getEmail())
                .fullName(googleUserInfo.getName())
                .role(UserRole.STUDENT)
                .department(null)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("New user has been created :)");

        return savedUser;
    }

    public void revokeRefreshToken(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(t -> {
            refreshTokenRepository.delete(t);
        });
    }

    private RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteExpiredByUserId(user.getUserId(), LocalDateTime.now());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenMaxAge));
        refreshToken.setCreatedAt(LocalDateTime.now());

        return refreshTokenRepository.save(refreshToken);
    }
}
