package com.acd.researchrepo.security;

import java.util.HashMap;
import java.util.Map;

import com.acd.researchrepo.model.User;
import com.acd.researchrepo.model.enums.UserRole;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Extract user information from JWT claims
        Integer userId = Integer.valueOf(jwt.getSubject());
        String email = jwt.getClaimAsString("email");
        String fullName = jwt.getClaimAsString("fullName");
        String roleString = jwt.getClaimAsString("role");
        UserRole role = roleString != null ? UserRole.valueOf(roleString) : null;

        // Create a basic User object with the extracted information
        User user = User.builder()
                .userId(userId)
                .email(email)
                .fullName(fullName)
                .role(role)
                .build();

        // Create attributes map to pass to CustomUserPrincipal
        Map<String, Object> attributes = new HashMap<>(jwt.getClaims());

        // Create CustomUserPrincipal
        CustomUserPrincipal principal = new CustomUserPrincipal(user, attributes);

        // Return JwtAuthenticationToken with our custom principal
        return new JwtAuthenticationToken(jwt, principal.getAuthorities()) {
            @Override
            public Object getPrincipal() {
                return principal;
            }
        };
    }
}
