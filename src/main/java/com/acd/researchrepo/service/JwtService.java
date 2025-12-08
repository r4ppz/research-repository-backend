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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiry:3600}")
    private int accessTokenExpirySeconds;

    public String generateAccessToken(User user) {
        LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(accessTokenExpirySeconds);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getUserId().toString());
        claims.put("email", user.getEmail());
        claims.put("fullName", user.getFullName());
        claims.put("role", user.getRole().name());

        if (user.getDepartment() != null) {
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
        return token;
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid or expired JWT token", e);
        }
    }

    public Integer getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return Integer.valueOf(claims.getSubject());
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}
