package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.userProfile.AssignProfileRequest;
import com.mabsplace.mabsplaceback.domain.dtos.userProfile.CreateUserProfileRequest;
import com.mabsplace.mabsplaceback.domain.dtos.userProfile.UpdateUserProfileRequest;
import com.mabsplace.mabsplaceback.domain.dtos.userProfile.UserProfileDTO;
import com.mabsplace.mabsplaceback.domain.services.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/userProfiles")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;
    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> createProfile(@Valid @RequestBody CreateUserProfileRequest request) {
        logger.info("Creating user profile with request: {}", request);
        UserProfileDTO profile = userProfileService.createProfile(request);
        logger.info("Created user profile: {}", profile);
        return new ResponseEntity<>(profile, HttpStatus.CREATED);
    }

    @PutMapping("/{profileId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @PathVariable Long profileId,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        logger.info("Updating user profile with ID: {}, Request: {}", profileId, request);
        UserProfileDTO profile = userProfileService.updateProfile(profileId, request);
        logger.info("Updated user profile: {}", profile);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/{profileId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long profileId) {
        logger.info("Deleting user profile with ID: {}", profileId);
        userProfileService.deleteProfile(profileId);
        logger.info("Deleted user profile successfully with ID: {}", profileId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> assignProfileToUser(@Valid @RequestBody AssignProfileRequest request) {
        logger.info("Assigning profile to user with request: {}", request);
        UserProfileDTO profile = userProfileService.assignProfileToUser(request);
        logger.info("Assigned profile to user successfully: {}", profile);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/users/{userId}/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeProfileFromUser(@PathVariable Long userId) {
        logger.info("Removing profile from user with ID: {}", userId);
        userProfileService.removeProfileFromUser(userId);
        logger.info("Removed profile from user successfully with ID: {}", userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'GET_USER_PROFILES')")
    @GetMapping
    public ResponseEntity<List<UserProfileDTO>> getAllProfiles() {
        logger.info("Fetching all user profiles");
        List<UserProfileDTO> profiles = userProfileService.getAllProfiles();
        logger.info("Fetched {} user profiles", profiles.size());
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/{profileId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Long profileId) {
        logger.info("Fetching user profile with ID: {}", profileId);
        UserProfileDTO profile = userProfileService.getProfile(profileId);
        logger.info("Fetched user profile: {}", profile);
        return ResponseEntity.ok(profile);
    }
}
