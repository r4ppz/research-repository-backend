package com.acd.researchrepo.service;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import com.acd.researchrepo.environment.AppProperties;
import com.acd.researchrepo.model.User;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotNull;

// This is JWT/access token generation class
// SpringSecurity handles most of the work like decoding, verifying etc.
// Refresh token generation is in AuthService since they are not JWTs
@Service
public class JwtService {

    private final AppProperties appProperties;

    private final String jwtSecret;
    private final int accessTokenExpirySeconds;

    private final SecretKey signingKey;

    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.jwtSecret = this.appProperties.getJwt().getSecret();
        this.accessTokenExpirySeconds = this.appProperties.getJwt().getAccessTokenExpiry();
        this.signingKey = Keys.hmacShaKeyFor(this.jwtSecret.getBytes());
    }

    public String generateAccessToken(@NotNull User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenExpirySeconds);

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("fullName", user.getFullName());
        claims.put("role", user.getRole().name());

        if (user.getDepartment() != null) {
            claims.put("departmentId", user.getDepartment().getDepartmentId());
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUserId().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .setIssuer("research-repo")
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }
}
