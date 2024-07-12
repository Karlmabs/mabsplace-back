package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.profile.ProfileRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Profile;
import com.mabsplace.mabsplaceback.domain.mappers.ProfileMapper;
import com.mabsplace.mabsplaceback.domain.repositories.ProfileRepository;
import com.mabsplace.mabsplaceback.domain.repositories.ServiceAccountRepository;
import com.mabsplace.mabsplaceback.domain.repositories.SubscriptionRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {

  private final ProfileRepository profileRepository;

  private final ProfileMapper mapper;
  private final ServiceAccountRepository serviceAccountRepository;
  private final SubscriptionRepository subscriptionRepository;

  public ProfileService(ProfileRepository profileRepository, ProfileMapper mapper, ServiceAccountRepository serviceAccountRepository, SubscriptionRepository subscriptionRepository) {
    this.profileRepository = profileRepository;
    this.mapper = mapper;
    this.serviceAccountRepository = serviceAccountRepository;
    this.subscriptionRepository = subscriptionRepository;
  }

  public Profile createProfile(ProfileRequestDto profile) throws ResourceNotFoundException{
    Profile newProfile = mapper.toEntity(profile);
    newProfile.setServiceAccount(serviceAccountRepository.findById(profile.getServiceAccountId()).orElseThrow(() -> new ResourceNotFoundException("ServiceAccount", "id", profile.getServiceAccountId())));
    if(profile.getSubscriptionId() != 0) {
      newProfile.setSubscription(subscriptionRepository.findById(profile.getSubscriptionId()).orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", profile.getSubscriptionId())));
    }
    return profileRepository.save(newProfile);
  }

  public void deleteProfile(Long id) {
    profileRepository.deleteById(id);
  }

  public Profile getProfileById(Long id) throws ResourceNotFoundException{
    return profileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Profile", "id", id));
  }

  public List<Profile> getAllProfiles() {
    return profileRepository.findAll();
  }

  public Profile updateProfile(Long id, ProfileRequestDto updatedProfile) throws ResourceNotFoundException{
    Profile target = profileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Profile", "id", id));
    Profile updated = mapper.partialUpdate(updatedProfile, target);
    updated.setServiceAccount(serviceAccountRepository.findById(updatedProfile.getServiceAccountId()).orElseThrow(() -> new ResourceNotFoundException("ServiceAccount", "id", updatedProfile.getServiceAccountId())));
    if(updatedProfile.getSubscriptionId() != 0) {
      updated.setSubscription(subscriptionRepository.findById(updatedProfile.getSubscriptionId()).orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", updatedProfile.getSubscriptionId())));
    }
    return profileRepository.save(updated);
  }

public List<Profile> getProfilesByServiceAccountId(Long serviceAccountId) {
    return profileRepository.findByServiceAccountId(serviceAccountId);
  }

}
