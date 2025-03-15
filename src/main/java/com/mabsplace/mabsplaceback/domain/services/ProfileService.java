package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.profile.ProfileRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Profile;
import com.mabsplace.mabsplaceback.domain.enums.ProfileStatus;
import com.mabsplace.mabsplaceback.domain.mappers.ProfileMapper;
import com.mabsplace.mabsplaceback.domain.repositories.ProfileRepository;
import com.mabsplace.mabsplaceback.domain.repositories.ServiceAccountRepository;
import com.mabsplace.mabsplaceback.domain.repositories.SubscriptionRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

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

    public Profile updateProfile(Long id, ProfileRequestDto updatedProfile) throws ResourceNotFoundException {
        Profile target = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", id));
        List<Profile> existingProfiles = profileRepository.findByServiceAccountId(updatedProfile.getServiceAccountId())
                .stream().filter(p -> !p.getId().equals(id)).toList();
        boolean nameExists = existingProfiles.stream()
                .anyMatch(p -> p.getProfileName().equalsIgnoreCase(updatedProfile.getProfileName()));
        if (nameExists) {
            throw new IllegalStateException("A profile with the same name already exists under this ServiceAccount.");
        }
        Profile updated = mapper.partialUpdate(updatedProfile, target);
        updated.setServiceAccount(serviceAccountRepository.findById(updatedProfile.getServiceAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("ServiceAccount", "id", updatedProfile.getServiceAccountId())));
        if (updatedProfile.getSubscriptionId() != 0) {
            updated.setSubscription(subscriptionRepository.findById(updatedProfile.getSubscriptionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", updatedProfile.getSubscriptionId())));
        }
        return profileRepository.save(updated);
    }

    public Profile createProfile(ProfileRequestDto profile) {
        logger.info("Creating profile with data: {}", profile);
        boolean nameExists = profileRepository.findByServiceAccountId(profile.getServiceAccountId())
                .stream().anyMatch(p -> p.getProfileName().equalsIgnoreCase(profile.getProfileName()));

        if (nameExists) {
            logger.warn("Attempted to create duplicate profile with name: {} under service account ID: {}", profile.getProfileName(), profile.getServiceAccountId());
            throw new IllegalStateException("A profile with the same name already exists under this ServiceAccount.");
        }

        Profile newProfile = mapper.toEntity(profile);
        newProfile.setServiceAccount(serviceAccountRepository.findById(profile.getServiceAccountId())
                .orElseThrow(() -> {
                    logger.error("ServiceAccount not found with ID: {}", profile.getServiceAccountId());
                    return new ResourceNotFoundException("ServiceAccount", "id", profile.getServiceAccountId());
                }));

        if (profile.getSubscriptionId() != 0) {
            newProfile.setSubscription(subscriptionRepository.findById(profile.getSubscriptionId())
                    .orElseThrow(() -> {
                        logger.error("Subscription not found with ID: {}", profile.getSubscriptionId());
                        return new ResourceNotFoundException("Subscription", "id", profile.getSubscriptionId());
                    }));
        }

        Profile savedProfile = profileRepository.save(newProfile);
        logger.info("Profile created successfully: {}", savedProfile);
        return savedProfile;
    }

    public void deleteProfile(Long id) {
        logger.info("Attempting to delete profile with ID: {}", id);
        Profile target = profileRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("Profile not found with ID: {}", id);
                return new ResourceNotFoundException("Profile", "id", id);
            });

        if ("active".equalsIgnoreCase(target.getStatus().toString())) {
            logger.warn("Cannot delete active profile with ID: {}", id);
            throw new IllegalStateException("Cannot delete a profile with status 'active'");
        }

        profileRepository.deleteById(id);
        logger.info("Profile deleted successfully with ID: {}", id);
    }

    public Profile getProfileById(Long id) throws ResourceNotFoundException {
        logger.info("Fetching profile by ID: {}", id);
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Profile not found with ID: {}", id);
                    return new ResourceNotFoundException("Profile", "id", id);
                });
        logger.info("Retrieved profile: {}", profile);
        return profile;
    }

    public List<Profile> getAllProfiles() {
        logger.info("Fetching all profiles");
        List<Profile> profiles = profileRepository.findAll();
        logger.info("Retrieved {} profiles", profiles.size());
        return profiles;
    }

    public List<Profile> getProfilesByServiceAccountId(Long serviceAccountId) {
        return profileRepository.findByServiceAccountId(serviceAccountId);
    }

}
