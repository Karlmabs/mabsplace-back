package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.userProfile.AssignProfileRequest;
import com.mabsplace.mabsplaceback.domain.dtos.userProfile.CreateUserProfileRequest;
import com.mabsplace.mabsplaceback.domain.dtos.userProfile.UpdateUserProfileRequest;
import com.mabsplace.mabsplaceback.domain.dtos.userProfile.UserProfileDTO;
import com.mabsplace.mabsplaceback.domain.entities.Role;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.entities.UserProfile;
import com.mabsplace.mabsplaceback.domain.repositories.ProfileRoleRepository;
import com.mabsplace.mabsplaceback.domain.repositories.RoleRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserProfileRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProfileRoleRepository profileRoleRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

    public UserProfileDTO createProfile(CreateUserProfileRequest request) {
        logger.info("Creating user profile with name: {}", request.getName());
        if (userProfileRepository.existsByName(request.getName())) {
            logger.warn("Profile creation failed, name already exists: {}", request.getName());
            throw new RuntimeException("Profile with name " + request.getName() + " already exists");
        }

        UserProfile profile = new UserProfile();
        profile.setName(request.getName());
        profile.setDescription(request.getDescription());

        Set<Role> roles = request.getRoleNames().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .collect(Collectors.toSet());

        profile.setRoles(roles);
        UserProfile savedProfile = userProfileRepository.save(profile);
        logger.info("User profile created successfully: {}", savedProfile.getId());

        return convertToDTO(savedProfile);
    }

    public UserProfileDTO updateProfile(Long profileId, UpdateUserProfileRequest request) {
        logger.info("Updating user profile with ID: {}", profileId);
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> {
                    logger.error("Profile not found with ID: {}", profileId);
                    return new RuntimeException("Profile not found");
                });

        if (request.getDescription() != null) {
            profile.setDescription(request.getDescription());
        }

        if (request.getRoleNames() != null) {
            Set<Role> roles = request.getRoleNames().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
            profile.setRoles(roles);
        }

        UserProfile updatedProfile = userProfileRepository.save(profile);
        logger.info("User profile updated successfully: {}", profileId);
        return convertToDTO(updatedProfile);
    }

    public void deleteProfile(Long profileId) {
        logger.info("Deleting user profile with ID: {}", profileId);
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> {
                    logger.error("Profile not found with ID: {}", profileId);
                    return new RuntimeException("Profile not found");
                });

        List<User> usersWithProfile = userRepository.findByUserProfile(profile);
        usersWithProfile.forEach(user -> {
            user.setUserProfile(null);
            userRepository.save(user);
            logger.info("Removed profile from user ID: {}", user.getId());
        });

        userProfileRepository.delete(profile);
        logger.info("User profile deleted successfully: {}", profileId);
    }

    public UserProfileDTO assignProfileToUser(AssignProfileRequest request) {
        logger.info("Assigning profile ID: {} to user ID: {}", request.getProfileId(), request.getUserId());
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", request.getUserId());
                    return new RuntimeException("User not found");
                });

        UserProfile profile = userProfileRepository.findById(request.getProfileId())
                .orElseThrow(() -> {
                    logger.error("Profile not found with ID: {}", request.getProfileId());
                    return new RuntimeException("Profile not found");
                });

        user.setUserProfile(profile);
        userRepository.save(user);
        logger.info("Profile ID: {} assigned to user ID: {}", request.getProfileId(), request.getUserId());

        return convertToDTO(profile);
    }

    public void removeProfileFromUser(Long userId) {
        logger.info("Removing profile from user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new RuntimeException("User not found");
                });

        user.setUserProfile(null);
        userRepository.save(user);
        logger.info("Profile removed successfully from user ID: {}", userId);
    }

    public List<UserProfileDTO> getAllProfiles() {
        logger.info("Fetching all user profiles");
        List<UserProfileDTO> profiles = userProfileRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("Fetched {} user profiles", profiles.size());
        return profiles;
    }

    public UserProfileDTO getProfile(Long profileId) {
        logger.info("Fetching user profile with ID: {}", profileId);
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> {
                    logger.error("Profile not found with ID: {}", profileId);
                    return new RuntimeException("Profile not found");
                });
        logger.info("Fetched user profile successfully: {}", profileId);
        return convertToDTO(profile);
    }

    private UserProfileDTO convertToDTO(UserProfile profile) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(profile.getId());
        dto.setName(profile.getName());
        dto.setDescription(profile.getDescription());
        dto.setRoleNames(profile.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());
        return dto;
    }
}
