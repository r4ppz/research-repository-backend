package com.acd.researchrepo.service;

import java.time.LocalDateTime;
import java.util.UUID;

import com.acd.researchrepo.dto.internal.AuthTokenContainer;
import com.acd.researchrepo.dto.internal.GoogleUserInfo;
import com.acd.researchrepo.dto.internal.RefreshResult;
import com.acd.researchrepo.environment.AppProperties;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.acd.researchrepo.mapper.UserMapper;
import com.acd.researchrepo.model.RefreshToken;
import com.acd.researchrepo.model.User;
import com.acd.researchrepo.repository.RefreshTokenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService {

    private final int refreshTokenMaxAge;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final GoogleAuthService googleAuthService;
    private final UserMapper userMapper;
    private final UserService userService;
    private final AppProperties appProperties;

    public AuthService(
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            GoogleAuthService googleAuthService,
            UserMapper userMapper,
            UserService userService,
            AppProperties appProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.googleAuthService = googleAuthService;
        this.userMapper = userMapper;
        this.userService = userService;
        this.appProperties = appProperties;

        this.refreshTokenMaxAge = this.appProperties.getToken().getRefreshTokenMaxAge();
    }

    /**
     * Authenticates a user using a Google authorization code.
     * Validates the code, retrieves or creates the user, generates new tokens, and
     * returns authentication data.
     *
     * @param googleAuthCode the Google authorization code to validate
     * @return AuthTokenContainer containing access token, refresh token, and user
     *         info
     */
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

    /**
     * Refreshes the access token using the provided refresh token value.
     * <p>
     * Validates the refresh token, deletes the old token, issues a new refresh
     * token,
     * and generates a new access token for the associated user.
     * </p>
     *
     * @param refreshTokenValue the value of the refresh token to use for refreshing
     * @return a {@link RefreshResult} containing the new access and refresh tokens
     * @throws ApiException if the refresh token is revoked or expired
     */
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

    /**
     * Revokes (deletes) the refresh token if it exists in the repository.
     *
     * @param refreshTokenValue the value of the refresh token to revoke
     */
    @Transactional
    public void revokeRefreshToken(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(refreshTokenRepository::delete);
    }

    /**
     * Creates and saves a new refresh token for the specified user.
     * Deletes any existing refresh tokens for the user before creating a new one.
     *
     * @param user the user for whom the refresh token is created
     * @return the newly created and saved RefreshToken
     */
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
