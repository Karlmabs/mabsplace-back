package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.userProfile.MigrationStatus;
import com.mabsplace.mabsplaceback.domain.entities.Role;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.entities.UserProfile;
import com.mabsplace.mabsplaceback.domain.repositories.RoleRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserProfileRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileMigrationService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;

    @PostConstruct
    public void migrateExistingUsers() {
        // Create default profiles if they don't exist
        UserProfile adminProfile = createProfileIfNotExists("ADMIN_PROFILE", "Administrator profile", "ROLE_ADMIN");
        UserProfile userProfile = createProfileIfNotExists("USER_PROFILE", "Default user profile", "ROLE_USER");

        // Get all users without profiles
        List<User> usersWithoutProfiles = userRepository.findByUserProfileIsNull();

        for (User user : usersWithoutProfiles) {
            Set<Role> userRoles = user.getRoles();

            // Determine which profile to assign based on roles
            if (userRoles.stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
                user.setUserProfile(adminProfile);
            } else {
                user.setUserProfile(userProfile);
            }

            // Save the updated user
            userRepository.save(user);
        }
    }

    private UserProfile createProfileIfNotExists(String name, String description, String... roleNames) {
        return userProfileRepository.findByName(name)
                .orElseGet(() -> {
                    UserProfile profile = new UserProfile();
                    profile.setName(name);
                    profile.setDescription(description);

                    Set<Role> roles = Arrays.stream(roleNames)
                            .map(roleName -> roleRepository.findByName(roleName)
                                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                            .collect(Collectors.toSet());

                    profile.setRoles(roles);
                    return userProfileRepository.save(profile);
                });
    }

    // Method to manually trigger migration if needed
    @Transactional
    public void performManualMigration() {
        migrateExistingUsers();
    }

    // Method to check migration status
    public MigrationStatus checkMigrationStatus() {
        long totalUsers = userRepository.count();
        long usersWithProfiles = userRepository.countByUserProfileIsNotNull();
        long usersWithoutProfiles = totalUsers - usersWithProfiles;

        return MigrationStatus.builder()
                .totalUsers(totalUsers)
                .usersWithProfiles(usersWithProfiles)
                .usersWithoutProfiles(usersWithoutProfiles)
                .migrationComplete(usersWithoutProfiles == 0)
                .build();
    }
}
