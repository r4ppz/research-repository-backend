package com.acd.researchrepo.config;

import java.util.List;

import javax.crypto.SecretKey;

import com.acd.researchrepo.environment.AppProperties;
import com.acd.researchrepo.security.CustomJwtAuthConverter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import io.jsonwebtoken.security.Keys;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AppProperties appProperties;
    private final CustomJwtAuthConverter customJwtAuthConverter;

    private final List<String> allowedOrigins;
    private final String jwtSecret;

    public SecurityConfig(AppProperties appProperties,
            CustomJwtAuthConverter customJwtAuthConverter) {
        this.appProperties = appProperties;
        this.customJwtAuthConverter = customJwtAuthConverter;
        this.allowedOrigins = this.appProperties.getCors().getAllowedOrigins();
        this.jwtSecret = this.appProperties.getJwt().getSecret();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        byte[] keyBytes = jwtSecret.getBytes();
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS512).build();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sessionManagement())
                .oauth2ResourceServer(oauth2ResourceServer())
                .authorizeHttpRequests(authorizeHttpRequests());
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private Customizer<OAuth2ResourceServerConfigurer<HttpSecurity>> oauth2ResourceServer() {
        return oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtAuthConverter));
    }

    private Customizer<SessionManagementConfigurer<HttpSecurity>> sessionManagement() {
        return sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    private Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> authorizeHttpRequests() {
        return auth -> auth
                // These are public endpoints, JWT/Accesstoken are not required.
                // But /refresh and /logout requires httpCookie/RefreshToken
                .requestMatchers("/api/auth/google", "/api/auth/refresh", "/api/auth/logout")
                .permitAll()
                // For endpoints docs library
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                .permitAll()
                // Other endpoint will require JWT/Accesstoken
                .anyRequest()
                .authenticated();
    }
}
