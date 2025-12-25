package com.acd.researchrepo.service;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import com.acd.researchrepo.model.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotNull;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final int accessTokenExpirySeconds;

    public JwtService(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.access-token-expiry}") int accessTokenExpirySeconds) {

        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.accessTokenExpirySeconds = accessTokenExpirySeconds;
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
