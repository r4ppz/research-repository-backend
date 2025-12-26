package com.acd.researchrepo.util;

import com.acd.researchrepo.model.enums.UserRole;
import com.acd.researchrepo.security.CustomUserPrincipal;

/**
 * Utility class for role-based access control.
 * Provides methods to check if a user has specific roles.
 */
public final class RoleBasedAccess {

    private RoleBasedAccess() {
    }

    public static boolean isUserStudentOrTeacher(CustomUserPrincipal user) {
        return hasRole(user, UserRole.STUDENT, UserRole.TEACHER);
    }

    public static boolean isUserAdmin(CustomUserPrincipal user) {
        return hasRole(user, UserRole.DEPARTMENT_ADMIN, UserRole.SUPER_ADMIN);
    }

    public static boolean isUserStudent(CustomUserPrincipal user) {
        return hasRole(user, UserRole.STUDENT);
    }

    public static boolean isUserTeacher(CustomUserPrincipal user) {
        return hasRole(user, UserRole.TEACHER);
    }

    public static boolean isUserDepartmentAdmin(CustomUserPrincipal user) {
        return hasRole(user, UserRole.DEPARTMENT_ADMIN);
    }

    public static boolean isUserSuperAdmin(CustomUserPrincipal user) {
        return hasRole(user, UserRole.SUPER_ADMIN);
    }

    private static boolean hasRole(CustomUserPrincipal user, UserRole... roles) {
        if (user == null) {
            return false;
        }
        UserRole userRole = user.getRole();
        for (UserRole role : roles) {
            if (userRole == role) {
                return true;
            }
        }
        return false;
    }
}
