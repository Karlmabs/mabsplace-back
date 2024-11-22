package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.*;
import com.mabsplace.mabsplaceback.domain.enums.ProfileStatus;
import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionMapper;
import com.mabsplace.mabsplaceback.domain.repositories.*;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import com.mabsplace.mabsplaceback.utils.Utils;
import jakarta.mail.MessagingException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
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
    private final EmailService emailService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, SubscriptionMapper mapper, UserRepository userRepository, SubscriptionPlanRepository subscriptionPlanRepository, ProfileRepository profileRepository, ServiceAccountService serviceAccountService, MyServiceService myServiceService, MyServiceRepository myServiceRepository, NotificationService notificationService, EmailService emailService) {
        this.subscriptionRepository = subscriptionRepository;
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.profileRepository = profileRepository;
        this.serviceAccountService = serviceAccountService;
        this.myServiceService = myServiceService;
        this.myServiceRepository = myServiceRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
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

        notificationService.sendNotificationToUser(newSubscription.getUser().getId(), "Subscription updated successfully", "Your subscription has been updated.", new HashMap<>());
        return subscriptionRepository.save(newSubscription);
    }

    public void deleteSubscription(Long id) {
        // free the profile if it's associated with the subscription before deleting it
        Subscription subscription = subscriptionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
        Profile profile = subscription.getProfile();
        if (profile != null) {
            profile.setStatus(ProfileStatus.INACTIVE);
            profileRepository.save(profile);
        }
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

        if (updatedSubscription.getProfileId() != 0L) {
            Profile newProfile = profileRepository.findById(updatedSubscription.getProfileId())
                    .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", updatedSubscription.getProfileId()));

            // Check if the profile is already active and not associated with this subscription
            if (newProfile.getStatus() == ProfileStatus.ACTIVE && !newProfile.equals(target.getProfile())) {
                throw new IllegalStateException("The profile is already active and cannot be used for a new subscription.");
            }

            // Update the new profile to ACTIVE and associate it with the subscription
            newProfile.setStatus(ProfileStatus.ACTIVE);
            profileRepository.save(newProfile);
            updated.setProfile(newProfile);

            // If there was an old profile and it's different from the new one, set it to INACTIVE
            if (target.getProfile() != null && !target.getProfile().equals(newProfile)) {
                Profile oldProfile = target.getProfile();
                oldProfile.setStatus(ProfileStatus.INACTIVE);
                profileRepository.save(oldProfile);
            }
        }

        notificationService.sendNotificationToUser(updated.getUser().getId(), "Subscription updated successfully", "Your subscription has been updated.", new HashMap<>());
        return subscriptionRepository.save(updated);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Runs every day at midnight
    public void expireSubscriptions() throws MessagingException {
        List<Subscription> subscriptions = subscriptionRepository.findByEndDateBeforeAndStatusNot(new Date(), SubscriptionStatus.EXPIRED);
        for (Subscription subscription : subscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            Profile profile = subscription.getProfile();
            if (profile != null) {
                profile.setStatus(ProfileStatus.INACTIVE);
                profileRepository.save(profile);
            }
            emailService.sendEmail("mabsplace2024@gmail.com", "Subscription Expired", "Subscription with id " + subscription.getId() + " has expired for user " + subscription.getUser().getUsername());
            subscriptionRepository.save(subscription);
        }
    }

    // Update Subscription Status
    public Subscription updateSubscriptionStatus(Long id, SubscriptionStatus newStatus){
        Subscription subscription = subscriptionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
        subscription.setStatus(newStatus);
        return subscriptionRepository.save(subscription);
    }

    // Extend Subscription
    public Subscription extendSubscription(Long id, int additionalDays){
        Subscription subscription = subscriptionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
        subscription.setEndDate(Utils.addDays(subscription.getEndDate(), additionalDays));
        return subscriptionRepository.save(subscription);
    }

    // Find Subscriptions by User
    public List<Subscription> findSubscriptionsByUser(Long userId){
        return subscriptionRepository.findByUserId(userId);
    }

    // Find Subscriptions by Status
    public List<Subscription> findSubscriptionsByStatus(SubscriptionStatus status){
        return subscriptionRepository.findByStatus(status);
    }

    // Bulk Update Subscriptions
    public List<Subscription> bulkUpdateSubscriptions(List<Long> subscriptionIds, SubscriptionRequestDto updatedData){
        List<Subscription> subscriptions = subscriptionRepository.findAllById(subscriptionIds);
        for (Subscription subscription : subscriptions) {
            mapper.partialUpdate(updatedData, subscription);
            subscriptionRepository.save(subscription);
        }
        return subscriptions;
    }

    // Renew Subscription
    public Subscription renewSubscription(Long id){
        Subscription subscription = subscriptionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
        subscription.setStartDate(Utils.getCurrentDate());
        subscription.setEndDate(Utils.addPeriod(subscription.getStartDate(), subscription.getSubscriptionPlan().getPeriod()));
        return subscriptionRepository.save(subscription);
    }

    public List<Subscription> getSubscriptionsByUserId(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }
}
