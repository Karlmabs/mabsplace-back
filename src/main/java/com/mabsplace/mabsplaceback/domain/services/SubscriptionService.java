package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.*;
import com.mabsplace.mabsplaceback.domain.enums.ProfileStatus;
import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionMapper;
import com.mabsplace.mabsplaceback.domain.repositories.*;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import com.mabsplace.mabsplaceback.utils.Utils;
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
  private final MyServiceService myServiceService;
  private final MyServiceRepository myServiceRepository;

  public SubscriptionService(SubscriptionRepository subscriptionRepository, SubscriptionMapper mapper, UserRepository userRepository, SubscriptionPlanRepository subscriptionPlanRepository, ProfileRepository profileRepository, ServiceAccountService serviceAccountService, MyServiceService myServiceService, MyServiceRepository myServiceRepository) {
    this.subscriptionRepository = subscriptionRepository;
    this.mapper = mapper;
    this.userRepository = userRepository;
    this.subscriptionPlanRepository = subscriptionPlanRepository;
    this.profileRepository = profileRepository;
    this.serviceAccountService = serviceAccountService;
    this.myServiceService = myServiceService;
    this.myServiceRepository = myServiceRepository;
  }


  public Subscription createSubscription(SubscriptionRequestDto subscription) throws ResourceNotFoundException {
    Subscription newSubscription = mapper.toEntity(subscription);
    newSubscription.setUser(userRepository.findById(subscription.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", subscription.getUserId())));

    SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findById(subscription.getSubscriptionPlanId()).orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", subscription.getSubscriptionPlanId()));

    newSubscription.setSubscriptionPlan(subscriptionPlan);
    MyService service = myServiceRepository.findById(subscription.getServiceId()).orElseThrow(() -> new ResourceNotFoundException("Service", "id", subscription.getServiceId()));

    newSubscription.setService(service);
    newSubscription.setStatus(SubscriptionStatus.ACTIVE);
    newSubscription.setEndDate(Utils.addPeriod(subscription.getStartDate(), subscriptionPlan.getPeriod()));

    List<ServiceAccount> availableServiceAccounts = myServiceService.getAvailableServiceAccounts(subscription.getServiceId());

    List<Profile> availableProfiles = serviceAccountService.getAvailableProfiles(availableServiceAccounts.getFirst().getId());

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

    MyService service = myServiceRepository.findById(updatedSubscription.getServiceId()).orElseThrow(() -> new ResourceNotFoundException("Service", "id", updatedSubscription.getServiceId()));

    updated.setService(service);

    List<ServiceAccount> availableServiceAccounts = myServiceService.getAvailableServiceAccounts(updatedSubscription.getServiceId());

    List<Profile> availableProfiles = serviceAccountService.getAvailableProfiles(availableServiceAccounts.getFirst().getId());

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
