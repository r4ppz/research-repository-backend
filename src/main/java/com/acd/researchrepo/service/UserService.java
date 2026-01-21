package com.acd.researchrepo.service;

import java.util.Objects;
import java.util.Optional;

import com.acd.researchrepo.config.PrivilegedUserConfig;
import com.acd.researchrepo.dto.internal.GoogleUserInfo;
import com.acd.researchrepo.model.Department;
import com.acd.researchrepo.model.User;
import com.acd.researchrepo.model.UserRole;
import com.acd.researchrepo.repository.DepartmentRepository;
import com.acd.researchrepo.repository.UserRepository;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    private UserRepository userRepository;
    private PrivilegedUserConfigLoader privilegedUserConfigLoader;
    private DepartmentRepository departmentRepository;

    public UserService(
            UserRepository userRepository,
            PrivilegedUserConfigLoader privilegedUserConfigLoader,
            DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.privilegedUserConfigLoader = privilegedUserConfigLoader;
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public User findOrCreateUser(GoogleUserInfo googleInfo) {
        PrivilegedUserConfig config = privilegedUserConfigLoader.getPrivilegedUserConfig();
        String email = normalizeEmail(googleInfo.getEmail());
        UserRole assignedRole = determineUserRole(email, config);
        Department assignedDepartment = resolveDepartment(email, config);

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            boolean updated = updateUserIfChanged(user, googleInfo, assignedRole, assignedDepartment);
            if (updated) {
                userRepository.save(user);
            }
            return user;
        } else {
            User newUser = User.builder()
                    .email(email)
                    .fullName(googleInfo.getName())
                    .profilePictureUrl(googleInfo.getProfilePictureUrl())
                    .role(assignedRole)
                    .department(assignedDepartment)
                    .build();
            return userRepository.save(newUser);
        }
    }

    private String normalizeEmail(String email) {
        return email.toLowerCase().trim();
    }

    private UserRole determineUserRole(String email, PrivilegedUserConfig config) {
        if (config.getSuperAdmins() != null && config.getSuperAdmins().contains(email)) {
            return UserRole.SUPER_ADMIN;
        }
        if (config.getTeachers() != null && config.getTeachers().contains(email)) {
            return UserRole.TEACHER;
        }
        if (config.getDepartmentAdminsMap().containsKey(email)) {
            return UserRole.DEPARTMENT_ADMIN;
        }
        return UserRole.STUDENT;
    }

    private Department resolveDepartment(String email, PrivilegedUserConfig config) {
        if (config.getDepartmentAdminsMap().containsKey(email)) {
            Integer departmentId = config.getDepartmentAdminsMap().get(email);
            if (departmentId != null) {
                return departmentRepository.findById(departmentId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No department with id: " + departmentId + " for user " + email));
            }
        }
        return null;
    }

    private boolean updateUserIfChanged(
            User user,
            GoogleUserInfo googleInfo,
            UserRole newRole,
            Department newDepartment) {

        boolean changed = false;

        if (!user.getRole().equals(newRole)) {
            user.setRole(newRole);
            changed = true;
        }

        if (!Objects.equals(user.getDepartment(), newDepartment)) {
            user.setDepartment(newDepartment);
            changed = true;
        }

        if (!user.getFullName().equals(googleInfo.getName())) {
            user.setFullName(googleInfo.getName());
            changed = true;
        }

        if (!Objects.equals(user.getProfilePictureUrl(), googleInfo.getProfilePictureUrl())) {
            user.setProfilePictureUrl(googleInfo.getProfilePictureUrl());
            changed = true;
        }

        return changed;
    }
}
