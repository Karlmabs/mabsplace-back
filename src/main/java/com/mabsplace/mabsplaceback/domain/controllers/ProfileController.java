package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.profile.ProfileRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.profile.ProfileResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Profile;
import com.mabsplace.mabsplaceback.domain.mappers.ProfileMapper;
import com.mabsplace.mabsplaceback.domain.services.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

  private final ProfileService profileService;

  private final ProfileMapper mapper;

  public ProfileController(ProfileService profileService, ProfileMapper mapper) {
    this.profileService = profileService;
    this.mapper = mapper;
  }

  @PostMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<ProfileResponseDto> createUser(@RequestBody ProfileRequestDto profileRequestDto) {
    Profile createdProfile = profileService.createProfile(profileRequestDto);
    return new ResponseEntity<>(mapper.toDto(createdProfile), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<ProfileResponseDto> getProfileById(@PathVariable Long id) {
    return ResponseEntity.ok(mapper.toDto(profileService.getProfileById(id)));
  }

  @GetMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<List<ProfileResponseDto>> getAllUsers() {
    List<Profile> Profiles = profileService.getAllProfiles();
    return new ResponseEntity<>(mapper.toDtoList(Profiles), HttpStatus.OK);
  }

  @PutMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<ProfileResponseDto> updateUser(@PathVariable Long id, @RequestBody ProfileRequestDto updatedProfile) {
    Profile updated = profileService.updateProfile(id, updatedProfile);
    if (updated != null) {
      return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @DeleteMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
    profileService.deleteProfile(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
