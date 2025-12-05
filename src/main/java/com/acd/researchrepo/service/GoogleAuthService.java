package com.acd.researchrepo.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;

import com.acd.researchrepo.dto.internal.GoogleUserInfo;
import com.acd.researchrepo.exception.DomainNotAllowedException;
import com.acd.researchrepo.exception.InvalidGoogleTokenException;
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

    @Value("${app.environment}")
    private String environment;

    private final GoogleAuthorizationCodeFlow GAuthorizationCodeFlow;
    private final GoogleIdTokenVerifier GIdTokenVerifier;

    public GoogleAuthService(@Value("${app.google.client-id}") String googleClientId,
            @Value("${app.google.client-secret}") String googleClientSecret,
            @Value("${app.google.redirect-uri}") String redirectUri) {

        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.redirectUri = redirectUri;

        // Create OAuth flow for exchanging authorization code for tokens
        this.GAuthorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                new GsonFactory(),
                googleClientId,
                googleClientSecret,
                Arrays.asList(
                        "openid",
                        "email",
                        "profile"))
                .setAccessType("offline")
                .build();

        // Create ID token verifier
        this.GIdTokenVerifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    /**
     * Exchange Google OAuth authorization code for user information
     */
    public GoogleUserInfo validateCodeAndGetUserInfo(String authorizationCode) {
        try {
            GoogleTokenResponse tokenResponse = GAuthorizationCodeFlow
                    .newTokenRequest(authorizationCode)
                    .setRedirectUri(redirectUri)
                    .execute();

            String idTokenString = tokenResponse.getIdToken();
            if (idTokenString == null) {
                throw new InvalidGoogleTokenException("No ID token received from Google");
            }

            GoogleIdToken idToken = GIdTokenVerifier.verify(idTokenString);
            if (idToken == null) {
                throw new InvalidGoogleTokenException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            if (!payload.getEmailVerified()) {
                throw new InvalidGoogleTokenException("Google email not verified");
            }

            // INFO: temp
            if (environment.equalsIgnoreCase("development")) {
                if (!email.endsWith(".com")) {
                    throw new DomainNotAllowedException("Email not allowed.  Must use .com");
                }
            } else {
                if (!email.endsWith("acdeducation.com")) {
                    throw new DomainNotAllowedException("Email domain not allowed.  Must use @acdeducation.com");
                }
            }

            return GoogleUserInfo.builder()
                    .email(email)
                    .name((String) payload.get("name"))
                    .googleId(payload.getSubject())
                    .build();

        } catch (IOException e) {
            throw new InvalidGoogleTokenException("Failed to communicate with Google", e);
        } catch (GeneralSecurityException e) {
            throw new InvalidGoogleTokenException("Failed to verify Google token", e);
        }
    }
}
