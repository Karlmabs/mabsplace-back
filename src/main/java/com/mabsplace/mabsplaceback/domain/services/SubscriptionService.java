package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.*;
import com.mabsplace.mabsplaceback.domain.enums.ProfileStatus;
import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionMapper;
import com.mabsplace.mabsplaceback.domain.repositories.*;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import com.mabsplace.mabsplaceback.notifications.service.NotificationService;
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
    private final NotificationService notificationService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, SubscriptionMapper mapper, UserRepository userRepository, SubscriptionPlanRepository subscriptionPlanRepository, ProfileRepository profileRepository, ServiceAccountService serviceAccountService, MyServiceService myServiceService, MyServiceRepository myServiceRepository, NotificationService notificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.profileRepository = profileRepository;
        this.serviceAccountService = serviceAccountService;
        this.myServiceService = myServiceService;
        this.myServiceRepository = myServiceRepository;
        this.notificationService = notificationService;
    }


    public Subscription createSubscription(SubscriptionRequestDto subscription) throws ResourceNotFoundException {
        Subscription newSubscription = mapper.toEntity(subscription);
        newSubscription.setUser(userRepository.findById(subscription.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", subscription.getUserId())));

        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findById(subscription.getSubscriptionPlanId()).orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", subscription.getSubscriptionPlanId()));
        newSubscription.setSubscriptionPlan(subscriptionPlan);

        MyService service = myServiceRepository.findById(subscription.getServiceId()).orElseThrow(() -> new ResourceNotFoundException("Service", "id", subscription.getServiceId()));
        newSubscription.setService(service);
        newSubscription.setStatus(subscription.getStatus());
        newSubscription.setEndDate(Utils.addPeriod(subscription.getStartDate(), subscriptionPlan.getPeriod()));

        if (subscription.getProfileId() != 0L) {
            Profile profile = profileRepository.findById(subscription.getProfileId()).orElseThrow(() -> new ResourceNotFoundException("Profile", "id", subscription.getProfileId()));

            // Check if the profile is already active
            if (profile.getStatus() == ProfileStatus.ACTIVE) {
                throw new IllegalStateException("The profile is already active and cannot be used for a new subscription.");
            }

            profile.setStatus(ProfileStatus.ACTIVE);
            profile = profileRepository.save(profile);
            newSubscription.setProfile(profile);
        }

        notificationService.sendNotificationToUser(newSubscription.getUser().getUsername(), "Subscription created successfully");
        return subscriptionRepository.save(newSubscription);
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
        updated.setStatus(updatedSubscription.getStatus());
        updated.setEndDate(Utils.addPeriod(updatedSubscription.getStartDate(), updated.getSubscriptionPlan().getPeriod()));

        if (updatedSubscription.getProfileId() != 0L){

            if(target.getProfile() != null && target.getProfile().getId() != updatedSubscription.getProfileId()){
                Profile profile = profileRepository.findById(updatedSubscription.getProfileId()).orElseThrow(() -> new ResourceNotFoundException("Profile", "id", updatedSubscription.getProfileId()));

                // Check if the profile is already active
                if (profile.getStatus() == ProfileStatus.ACTIVE) {
                    throw new IllegalStateException("The profile is already active and cannot be used for a new subscription.");
                }

                profile.setStatus(ProfileStatus.ACTIVE);
                profile = profileRepository.save(profile);
                updated.setProfile(profile);

                Profile oldProfile = target.getProfile();
                if (oldProfile != null && !oldProfile.getId().equals(profile.getId())) {
                    oldProfile.setStatus(ProfileStatus.INACTIVE);
                    profileRepository.save(oldProfile);
                }
            }


        }

        notificationService.sendNotificationToUser(updated.getUser().getUsername(), "Subscription updated successfully");
        return subscriptionRepository.save(updated);
    }
}
