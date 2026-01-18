package com.acd.researchrepo.util;

import java.util.Arrays;

import com.acd.researchrepo.environment.AppProperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class CookieUtil {

    private final String environment;
    private final String refreshTokenName;
    private final int refreshTokenMaxAge;

    public CookieUtil(
            AppProperties appProperties,
            @Value("${spring.profiles.active:}") String environment) {
        this.environment = environment;
        this.refreshTokenName = appProperties.getToken().getRefreshTokenCookieName();
        this.refreshTokenMaxAge = appProperties.getToken().getRefreshTokenMaxAge();
    }

    /**
     * Sets the refresh token cookie using modern ResponseCookie to support SameSite
     * attributes.
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        addCookie(response, refreshToken, refreshTokenMaxAge);
    }

    /**
     * Clears the refresh token cookie by setting max-age to 0.
     */
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        addCookie(response, "", 0);
    }

    /**
     * Extracts the refresh token from the request cookies.
     */
    public String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null)
            return null;

        return Arrays.stream(request.getCookies())
                .filter(c -> refreshTokenName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Internal helper to build and add the cookie header.
     * Logic: In "prod", use SameSite=None and Secure=true for cross-site (GitHub ->
     * Tailscale).
     */
    private void addCookie(HttpServletResponse response, String value, int maxAge) {
        boolean isProd = "prod".equalsIgnoreCase(environment);

        ResponseCookie cookie = ResponseCookie.from(refreshTokenName, value)
                .httpOnly(true)
                .secure(isProd) // Required for SameSite=None
                .path("/")
                .maxAge(maxAge)
                .sameSite(isProd ? "None" : "Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        log.debug("Cookie set: Name={}, MaxAge={}, Secure={}, SameSite={}",
                refreshTokenName, maxAge, cookie.isSecure(), cookie.getSameSite());
    }
}
