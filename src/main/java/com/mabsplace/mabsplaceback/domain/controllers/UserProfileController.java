package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.userProfile.AssignProfileRequest;
import com.mabsplace.mabsplaceback.domain.dtos.userProfile.CreateUserProfileRequest;
import com.mabsplace.mabsplaceback.domain.dtos.userProfile.UpdateUserProfileRequest;
import com.mabsplace.mabsplaceback.domain.dtos.userProfile.UserProfileDTO;
import com.mabsplace.mabsplaceback.domain.services.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> createProfile(@Valid @RequestBody CreateUserProfileRequest request) {
        UserProfileDTO profile = userProfileService.createProfile(request);
        return new ResponseEntity<>(profile, HttpStatus.CREATED);
    }

    @PutMapping("/{profileId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @PathVariable Long profileId,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        UserProfileDTO profile = userProfileService.updateProfile(profileId, request);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/{profileId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long profileId) {
        userProfileService.deleteProfile(profileId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> assignProfileToUser(@Valid @RequestBody AssignProfileRequest request) {
        UserProfileDTO profile = userProfileService.assignProfileToUser(request);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/users/{userId}/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeProfileFromUser(@PathVariable Long userId) {
        userProfileService.removeProfileFromUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileDTO>> getAllProfiles() {
        List<UserProfileDTO> profiles = userProfileService.getAllProfiles();
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/{profileId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Long profileId) {
        UserProfileDTO profile = userProfileService.getProfile(profileId);
        return ResponseEntity.ok(profile);
    }
}
