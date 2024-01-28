package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Profile;
import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.enums.ProfileStatus;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionMapper;
import com.mabsplace.mabsplaceback.domain.repositories.ProfileRepository;
import com.mabsplace.mabsplaceback.domain.repositories.SubscriptionPlanRepository;
import com.mabsplace.mabsplaceback.domain.repositories.SubscriptionRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;
  private final SubscriptionMapper mapper;
  private final UserRepository userRepository;
  private final SubscriptionPlanRepository subscriptionPlanRepository;
  private final ProfileRepository profileRepository;

  private final ServiceAccountService serviceAccountService;

  public SubscriptionService(SubscriptionRepository subscriptionRepository, SubscriptionMapper mapper, UserRepository userRepository, SubscriptionPlanRepository subscriptionPlanRepository, ProfileRepository profileRepository, ServiceAccountService serviceAccountService) {
    this.subscriptionRepository = subscriptionRepository;
    this.mapper = mapper;
    this.userRepository = userRepository;
    this.subscriptionPlanRepository = subscriptionPlanRepository;
    this.profileRepository = profileRepository;
    this.serviceAccountService = serviceAccountService;
  }

  public Subscription createSubscription(SubscriptionRequestDto subscription) throws ResourceNotFoundException {
    Subscription newSubscription = mapper.toEntity(subscription);
    newSubscription.setUser(userRepository.findById(subscription.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", subscription.getUserId())));
    newSubscription.setSubscriptionPlan(subscriptionPlanRepository.findById(subscription.getSubscriptionPlanId()).orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", subscription.getSubscriptionPlanId())));

    List<Profile> availableProfiles = serviceAccountService.getAvailableProfiles(subscription.getServiceAccountId());

    if (!availableProfiles.isEmpty()) {
      Profile profile = profileRepository.findById(availableProfiles.getFirst().getId()).orElseThrow(() -> new ResourceNotFoundException("Profile", "id", availableProfiles.getFirst().getId()));
      profile.setStatus(ProfileStatus.ACTIVE);
      profile = profileRepository.save(profile);

      newSubscription.setProfile(profile);
      return subscriptionRepository.save(newSubscription);
    } else
      throw new RuntimeException("No available profiles for this subscription");
  }

  public void deleteSubscription(Long id) {
    subscriptionRepository.deleteById(id);
  }

  public Subscription getSubscriptionById(Long id) throws ResourceNotFoundException {
    return subscriptionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
  }

  public List<Subscription> getAllSubscriptions() {
    return subscriptionRepository.findAll();
  }

  public Subscription updateSubscription(Long id, SubscriptionRequestDto updatedSubscription) {
    Subscription target = subscriptionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
    Subscription updated = mapper.partialUpdate(updatedSubscription, target);
    updated.setUser(userRepository.findById(updatedSubscription.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", updatedSubscription.getUserId())));
    updated.setSubscriptionPlan(subscriptionPlanRepository.findById(updatedSubscription.getSubscriptionPlanId()).orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", updatedSubscription.getSubscriptionPlanId())));

    List<Profile> availableProfiles = serviceAccountService.getAvailableProfiles(updatedSubscription.getServiceAccountId());

    if (!availableProfiles.isEmpty()) {
      Profile profile = profileRepository.findById(availableProfiles.getFirst().getId()).orElseThrow(() -> new ResourceNotFoundException("Profile", "id", availableProfiles.getFirst().getId()));
      profile.setStatus(ProfileStatus.ACTIVE);
      profile = profileRepository.save(profile);

      updated.setProfile(profile);
      return subscriptionRepository.save(updated);
    } else
      throw new RuntimeException("No available profiles for this subscription");
  }
}
