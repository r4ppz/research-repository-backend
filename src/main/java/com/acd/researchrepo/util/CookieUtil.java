package com.acd.researchrepo.util;

import com.acd.researchrepo.environment.AppProperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtil {

    private final String environment;
    private final String refreshTokenName;
    private final int refreshTokenMaxAge;

    AppProperties appProperties;

    public CookieUtil(
            AppProperties appProperties,
            @Value("${spring.profiles.active:}") String environment) {
        this.environment = environment;
        this.appProperties = appProperties;

        this.refreshTokenName = appProperties.getToken().getRefreshTokenCookieName();
        this.refreshTokenMaxAge = appProperties.getToken().getRefreshTokenMaxAge();
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(refreshTokenName, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(refreshTokenMaxAge);
        if ("prod".equalsIgnoreCase(environment)) {
            cookie.setSecure(true);
            cookie.setAttribute("SameSite", "None");
        } else {
            cookie.setSecure(false);
            cookie.setAttribute("SameSite", "Lax");
        }
        response.addCookie(cookie);
    }

    public String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (refreshTokenName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(refreshTokenName, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        if ("prod".equalsIgnoreCase(environment)) {
            cookie.setSecure(true);
            cookie.setAttribute("SameSite", "None");
        } else {
            cookie.setSecure(false);
            cookie.setAttribute("SameSite", "Lax");
        }
        response.addCookie(cookie);
    }
}
