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

    public UserProfileDTO createProfile(CreateUserProfileRequest request) {
        if (userProfileRepository.existsByName(request.getName())) {
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

        return convertToDTO(savedProfile);
    }

    public UserProfileDTO updateProfile(Long profileId, UpdateUserProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

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
        return convertToDTO(updatedProfile);
    }

    public void deleteProfile(Long profileId) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        // First, remove this profile from all users that have it
        List<User> usersWithProfile = userRepository.findByUserProfile(profile);
        usersWithProfile.forEach(user -> {
            user.setUserProfile(null);
            userRepository.save(user);
        });

        userProfileRepository.delete(profile);
    }

    public UserProfileDTO assignProfileToUser(AssignProfileRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        user.setUserProfile(profile);
        userRepository.save(user);

        return convertToDTO(profile);
    }

    public void removeProfileFromUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUserProfile(null);
        userRepository.save(user);
    }

    public List<UserProfileDTO> getAllProfiles() {
        return userProfileRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserProfileDTO getProfile(Long profileId) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
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
