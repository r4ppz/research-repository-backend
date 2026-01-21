package com.acd.researchrepo.security;

import java.util.HashMap;
import java.util.Map;

import com.acd.researchrepo.model.Department;
import com.acd.researchrepo.model.User;
import com.acd.researchrepo.model.UserRole;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CustomJwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Integer userId = Integer.valueOf(jwt.getSubject());
        String email = jwt.getClaimAsString("email");
        String fullName = jwt.getClaimAsString("fullName");
        String roleString = jwt.getClaimAsString("role");
        UserRole role = roleString != null ? UserRole.valueOf(roleString) : null;
        String profilePictureUrl = jwt.getClaimAsString("profilePictureUrl");

        // Extract departmentId from JWT and create minimal Department object
        Department department = null;
        Number departmentIdNumber = jwt.getClaim("departmentId");
        if (departmentIdNumber != null) {
            department = new Department();
            department.setDepartmentId(departmentIdNumber.intValue());
        }

        User user = User.builder()
                .userId(userId)
                .email(email)
                .fullName(fullName)
                .role(role)
                .department(department)
                .profilePictureUrl(profilePictureUrl)
                .build();

        Map<String, Object> attributes = new HashMap<>(jwt.getClaims());

        CustomUserPrincipal principal = new CustomUserPrincipal(user, attributes);

        return new JwtAuthenticationToken(jwt, principal.getAuthorities()) {
            @Override
            public Object getPrincipal() {
                return principal;
            }
        };
    }
}
