package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.entities.Profile;
import com.mabsplace.mabsplaceback.domain.repositories.ProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

  private final ProfileRepository profileRepository;

  public ProfileService(ProfileRepository profileRepository) {
    this.profileRepository = profileRepository;
  }

  public Profile createProfile(Profile profile) {
    return profileRepository.save(profile);
  }

  public Profile getProfile(Long id) {
    return profileRepository.findById(id).orElse(null);
  }

  public Profile updateProfile(Profile profile) {
    return profileRepository.save(profile);
  }

  public void deleteProfile(Long id) {
    profileRepository.deleteById(id);
  }
}
