package com.acd.researchrepo.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;

import com.acd.researchrepo.dto.internal.GoogleUserInfo;
import com.acd.researchrepo.exception.ApiException;
import com.acd.researchrepo.exception.ErrorCode;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GoogleAuthService {

    @Value("${app.google.client-id}")
    private String googleClientId;

    @Value("${app.google.client-secret}")
    private String googleClientSecret;

    @Value("${app.google.redirect-uri}")
    private String redirectUri;

    @Value("${spring.profiles.active}")
    private String environment;

    private final GoogleAuthorizationCodeFlow authorizationFlow;
    private final GoogleIdTokenVerifier idTokenVerifier;

    public GoogleAuthService(
            @Value("${app.google.client-id}") String googleClientId,
            @Value("${app.google.client-secret}") String googleClientSecret,
            @Value("${app.google.redirect-uri}") String redirectUri,
            @Value("${spring.profiles.active}") String environment) {

        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.redirectUri = redirectUri;
        this.environment = environment;

        NetHttpTransport transport = new NetHttpTransport();
        GsonFactory jsonFactory = new GsonFactory();

        this.authorizationFlow = new GoogleAuthorizationCodeFlow.Builder(
                transport,
                jsonFactory,
                googleClientId,
                googleClientSecret,
                Arrays.asList("openid", "email", "profile"))
                .setAccessType("offline")
                .build();

        this.idTokenVerifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    public GoogleUserInfo validateCodeAndGetUserInfo(String authorizationCode) {
        GoogleTokenResponse tokenResponse = exchangeAuthorizationCode(authorizationCode);
        GoogleIdToken.Payload payload = verifyAndExtractPayload(tokenResponse.getIdToken());

        String email = getVerifiedEmail(payload);
        enforceDomainRestrictions(email);

        return GoogleUserInfo.builder()
                .email(email)
                .name((String) payload.get("name"))
                .googleId(payload.getSubject())
                .build();
    }

    private GoogleTokenResponse exchangeAuthorizationCode(String code) {
        try {
            return authorizationFlow
                    .newTokenRequest(code)
                    .setRedirectUri(redirectUri)
                    .execute();
        } catch (IOException e) {
            throw new ApiException(ErrorCode.INVALID_TOKEN, "Failed to exchange Google authorization code");
        }
    }

    private GoogleIdToken.Payload verifyAndExtractPayload(String idTokenString) {
        if (idTokenString == null) {
            throw new ApiException(ErrorCode.INVALID_TOKEN, "ID token missing from Google response");
        }

        GoogleIdToken idToken;
        try {
            idToken = idTokenVerifier.verify(idTokenString);
        } catch (GeneralSecurityException | IOException e) {
            throw new ApiException(ErrorCode.INVALID_TOKEN, "Failed to verify Google ID token");
        }

        if (idToken == null) {
            throw new ApiException(ErrorCode.INVALID_TOKEN, "Invalid Google ID token");
        }

        return idToken.getPayload();
    }

    private void enforceDomainRestrictions(String email) {
        if ("prod".equalsIgnoreCase(environment)) {
            if (!email.endsWith("acdeducation.com")) {
                throw new ApiException(ErrorCode.DOMAIN_NOT_ALLOWED, "Email domain must be @acdeducation.com");
            }
        } else {
            if (!email.endsWith(".com")) {
                throw new ApiException(ErrorCode.DOMAIN_NOT_ALLOWED, "Development mode only allows .com emails");
            }
        }
    }

    private String getVerifiedEmail(GoogleIdToken.Payload payload) {
        if (!payload.getEmailVerified()) {
            throw new ApiException(ErrorCode.INVALID_TOKEN, "Google email is not verified");
        }
        String email = payload.getEmail();
        if (email == null) {
            throw new ApiException(ErrorCode.INVALID_TOKEN, "Google email is null");
        }
        return email;
    }
}
