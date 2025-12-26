package com.acd.researchrepo.util;

import com.acd.researchrepo.environment.AppProperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtil {

    AppProperties appProperties;

    private final String environment;
    private final String refreshTokenName;
    private final int refreshTokenMaxAge;

    public CookieUtil(
            AppProperties appProperties,
            @Value("${spring.profiles.active:}") String environment) {
        this.environment = environment;
        this.appProperties = appProperties;
        this.refreshTokenName = this.appProperties.getToken().getRefreshTokenCookieName();
        this.refreshTokenMaxAge = this.appProperties.getToken().getRefreshTokenMaxAge();
    }

    /**
     * Sets a refresh token as an HTTP cookie in the response.
     *
     * @param response     The HttpServletResponse to which the cookie will be
     *                     added.
     * @param refreshToken The refresh token to be stored in the cookie.
     */
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

    /**
     * Extracts the refresh token from the cookies in the provided HTTP request.
     *
     * @param request the HttpServletRequest containing the cookies
     * @return the value of the refresh token cookie if present, or {@code null} if
     *         not found
     */
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

    /**
     * Clears the refresh token cookie by setting its value to an empty string,
     * marking it as HTTP-only, and setting its maximum age to 0 to expire it
     * immediately.
     *
     * The cookie's security attributes are configured based on the environment:
     * - In "prod" environment: Secure flag is set to true, and SameSite is set to
     * "None".
     * - In other environments: Secure flag is set to false, and SameSite is set to
     * "Lax".
     *
     * @param response the HttpServletResponse to which the cookie will be added
     */
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
