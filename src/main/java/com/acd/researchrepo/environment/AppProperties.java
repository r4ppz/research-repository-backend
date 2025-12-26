package com.acd.researchrepo.environment;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Configuration properties for the application
 */
@ConfigurationProperties(prefix = "app")
@Validated
@Getter
@RequiredArgsConstructor
public class AppProperties {

    @Valid
    @NotNull
    private final Google google;
    @Valid
    @NotNull
    private final Jwt jwt;
    @Valid
    @NotNull
    private final Token token;
    @Valid
    @NotNull
    private final Cors cors;

    @Getter
    @RequiredArgsConstructor
    public static class Google {
        @NotBlank(message = "APP_GOOGLE_CLIENT_ID must be set.")
        private final String clientId;
        @NotBlank(message = "APP_GOOGLE_CLIENT_SECRET must be set.")
        private final String clientSecret;
        @NotBlank(message = "APP_GOOGLE_REDIRECT_URI must be set.")
        private final String redirectUri;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Jwt {
        @NotBlank(message = "APP_JWT_SECRET must be set and cannot be blank.")
        private final String secret;

        @NotNull(message = "APP_JWT_ACCESS_TOKEN_EXPIRY must be set.")
        private final int accessTokenExpiry;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Token {
        @NotBlank
        private final String refreshTokenCookieName;
        @NotNull
        private final int refreshTokenMaxAge;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Cors {
        @NotNull
        private final List<String> allowedOrigins;
        @NotNull
        private final List<String> allowedMethods;
        @NotNull
        private final List<String> allowedHeaders;
        private final boolean allowCredentials;
    }
}
