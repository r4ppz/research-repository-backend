package com.acd.researchrepo.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import com.acd.researchrepo.exception.InvalidTokenException;
import com.acd.researchrepo.model.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import lombok.extern.slf4j.Slf4j;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotNull;

@Service
@Validated
@Slf4j
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiry}")
    private int accessTokenExpirySeconds;

    public String generateAccessToken(@NotNull User user) {
        log.debug("Generating access token for user ID: {}", user.getUserId());
        LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(accessTokenExpirySeconds);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getUserId().toString());
        claims.put("email", user.getEmail());
        claims.put("fullName", user.getFullName());
        claims.put("role", user.getRole().name());

        if (user.getDepartment() != null) {
            log.debug("Including department in claims for user ID: {}", user.getUserId());
            claims.put("departmentId", user.getDepartment().getDepartmentId());
        }

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUserId().toString())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(expiryTime.toInstant(ZoneOffset.UTC)))
                .setIssuer("research-repo")
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
        log.info("Access token generated successfully for user ID: {}", user.getUserId());
        return token;
    }

    public Claims validateToken(@NotNull String token) {
        log.debug("Entering validateToken method");
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            log.debug("JWT token validated successfully");
            return claims;
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid or expired JWT token", e);
        }
    }

    public Integer getUserIdFromToken(@NotNull String token) {
        Claims claims = validateToken(token);
        return Integer.valueOf(claims.getSubject());
    }

    public boolean isTokenExpired(@NotNull String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            log.warn("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}
