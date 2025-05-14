package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.profile.ProfileRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.profile.ProfileResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Profile;
import com.mabsplace.mabsplaceback.domain.mappers.ProfileMapper;
import com.mabsplace.mabsplaceback.domain.services.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final ProfileService profileService;

    private final ProfileMapper mapper;

    public ProfileController(ProfileService profileService, ProfileMapper mapper) {
        this.profileService = profileService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ProfileResponseDto> createUser(@RequestBody ProfileRequestDto profileRequestDto) {
        logger.info("Creating profile with request: {}", profileRequestDto);
        Profile createdProfile = profileService.createProfile(profileRequestDto);
        logger.info("Profile created: {}", createdProfile);
        return new ResponseEntity<>(mapper.toDto(createdProfile), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponseDto> getProfileById(@PathVariable Long id) {
        logger.info("Fetching profile with ID: {}", id);
        Profile profile = profileService.getProfileById(id);
        logger.info("Fetched profile: {}", profile);
        return ResponseEntity.ok(mapper.toDto(profile));
    }

    @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'GET_PROFILES')")
    @GetMapping
    public ResponseEntity<List<ProfileResponseDto>> getAllProfiles() {
        logger.info("Fetching all profiles");
        List<Profile> profiles = profileService.getAllProfiles();
        logger.info("Fetched {} profiles", profiles.size());
        return ResponseEntity.ok(mapper.toDtoList(profiles));
    }

    @GetMapping("/serviceAccount/{serviceId}")
    public ResponseEntity<List<ProfileResponseDto>> getProfilesByServiceAccountId(@PathVariable Long serviceId) {
        logger.info("Fetching profiles by service ID: {}", serviceId);
        List<Profile> profiles = profileService.getProfilesByServiceAccountId(serviceId);
        logger.info("Fetched {} profiles for service ID: {}", profiles.size(), serviceId);
        return ResponseEntity.ok(mapper.toDtoList(profiles));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfileResponseDto> updateProfile(@PathVariable Long id, @RequestBody ProfileRequestDto updatedProfile) {
        logger.info("Updating profile with ID: {}, Request: {}", id, updatedProfile);
        Profile profile = profileService.updateProfile(id, updatedProfile);
        if (profile != null) {
            logger.info("Updated profile successfully: {}", profile);
            return ResponseEntity.ok(mapper.toDto(profile));
        }
        logger.warn("Profile not found with ID: {}", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        logger.info("Deleting profile with ID: {}", id);
        profileService.deleteProfile(id);
        logger.info("Deleted profile successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
