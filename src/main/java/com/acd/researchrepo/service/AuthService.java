package com.acd.researchrepo.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.acd.researchrepo.dto.AuthResponse;
import com.acd.researchrepo.dto.AuthResponseWithRefreshToken;
import com.acd.researchrepo.mapper.UserMapper;
import com.acd.researchrepo.model.RefreshToken;
import com.acd.researchrepo.model.User;
import com.acd.researchrepo.model.enums.UserRole;
import com.acd.researchrepo.repository.RefreshTokenRepository;
import com.acd.researchrepo.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

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

    /**
     * Authenticate user with Google OAuth code
     * This is the main method that handles POST /api/auth/google
     */
    @Transactional
    public AuthResponse authenticateWithGoogle(String googleAuthCode) {
        GoogleUserInfo googleUserInfo = googleAuthService.validateCodeAndGetUserInfo(googleAuthCode);
        User user = findOrCreateUser(googleUserInfo);
        String accessToken = jwtService.generateAccessToken(user);

        // INFO: refresh token will be set as cookie by controller

        return AuthResponse.builder()
                .accessToken(accessToken)
                .user(userMapper.toDto(user))
                .build();
    }

    /**
     * Get the refresh token string from the AuthResponse for cookie setting
     * We need this in the controller to set the HttpOnly cookie
     */
    @Transactional
    public AuthResponseWithRefreshToken authenticateWithGoogleAndGetRefreshToken(String googleAuthCode) {
        GoogleUserInfo googleUserInfo = googleAuthService.validateCodeAndGetUserInfo(googleAuthCode);
        User user = findOrCreateUser(googleUserInfo);
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user);
        return AuthResponseWithRefreshToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(userMapper.toDto(user))
                .build();
    }

    private User findOrCreateUser(GoogleUserInfo googleUserInfo) {
        Optional<User> existingUser = userRepository.findByEmail(googleUserInfo.getEmail());

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User newUser = User.builder()
                .email(googleUserInfo.getEmail())
                .fullName(googleUserInfo.getName())
                .role(UserRole.STUDENT)
                .department(null)
                .build();

        User savedUser = userRepository.save(newUser);

        return savedUser;
    }

    private RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUserId(user.getUserId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(30));

        return refreshTokenRepository.save(refreshToken);
    }
}
