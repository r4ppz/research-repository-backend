package com.acd.researchrepo.service;

import java.time.LocalDateTime;
import java.util.UUID;

import com.acd.researchrepo.dto.internal.AuthTokenContainer;
import com.acd.researchrepo.dto.internal.GoogleUserInfo;
import com.acd.researchrepo.dto.internal.RefreshResult;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.mapper.UserMapper;
import com.acd.researchrepo.model.RefreshToken;
import com.acd.researchrepo.model.User;
import com.acd.researchrepo.repository.RefreshTokenRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService {

    @Value("${app.refresh-token.max-age:2592000}")
    private int refreshTokenMaxAge;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final GoogleAuthService googleAuthService;
    private final UserMapper userMapper;
    private final UserService userService;

    public AuthService(
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            GoogleAuthService googleAuthService,
            UserMapper userMapper,
            UserService userService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.googleAuthService = googleAuthService;
        this.userMapper = userMapper;
        this.userService = userService;
    }

    @Transactional
    public AuthTokenContainer authenticateWithGoogle(String googleAuthCode) {
        GoogleUserInfo googleUserInfo = googleAuthService.validateCodeAndGetUserInfo(googleAuthCode);

        User user = userService.findOrCreateUser(googleUserInfo);
        RefreshToken newRefresh = createRefreshToken(user);
        String accessToken = jwtService.generateAccessToken(user);

        return AuthTokenContainer.builder()
                .accessToken(accessToken)
                .refreshToken(newRefresh.getToken())
                .user(userMapper.toDto(user))
                .build();
    }

    @Transactional
    public RefreshResult refreshAccessToken(String refreshTokenValue) {
        LocalDateTime now = LocalDateTime.now();

        RefreshToken oldToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new ApiException(ErrorCode.REFRESH_TOKEN_REVOKED));

        if (oldToken.getExpiresAt().isBefore(now)) {
            refreshTokenRepository.delete(oldToken);
            throw new ApiException(ErrorCode.REFRESH_TOKEN_REVOKED);
        }

        User user = oldToken.getUser();
        refreshTokenRepository.delete(oldToken);

        RefreshToken newToken = createRefreshToken(user);
        String newAccessToken = jwtService.generateAccessToken(user);

        return RefreshResult.builder()
                .accessToken(newAccessToken)
                .refreshToken(newToken.getToken())
                .build();
    }

    @Transactional
    public void revokeRefreshToken(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(refreshTokenRepository::delete);
    }

    @Transactional
    private RefreshToken createRefreshToken(User user) {
        LocalDateTime now = LocalDateTime.now();

        refreshTokenRepository.deleteByUserId(user.getUserId());

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setCreatedAt(now);
        token.setExpiresAt(now.plusSeconds(refreshTokenMaxAge));

        return refreshTokenRepository.save(token);
    }
}
