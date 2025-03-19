package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.email.EmailRequest;
import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.MyService;
import com.mabsplace.mabsplaceback.domain.entities.Profile;
import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import com.mabsplace.mabsplaceback.domain.enums.ProfileStatus;
import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionMapper;
import com.mabsplace.mabsplaceback.domain.repositories.*;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import com.mabsplace.mabsplaceback.utils.Utils;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class SubscriptionService {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);
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
    private final WalletService walletService;
    private final SubscriptionPaymentOrchestrator orchestrator;

    private final SubscriptionPaymentOrchestrator subscriptionPaymentOrchestrator;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, SubscriptionMapper mapper, UserRepository userRepository, SubscriptionPlanRepository subscriptionPlanRepository, ProfileRepository profileRepository, ServiceAccountService serviceAccountService, MyServiceService myServiceService, MyServiceRepository myServiceRepository, NotificationService notificationService, EmailService emailService, WalletService walletService, SubscriptionPaymentOrchestrator orchestrator, SubscriptionPaymentOrchestrator subscriptionPaymentOrchestrator) {
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
        this.walletService = walletService;
        this.orchestrator = orchestrator;
        this.subscriptionPaymentOrchestrator = subscriptionPaymentOrchestrator;
    }

    @Scheduled(cron = "0 0 0 * * *") // Runs daily at midnight
    public void processSubscriptionRenewals() throws MessagingException {
        logger.info("Starting daily subscription renewal process");
        Date today = new Date();
        List<Subscription> subscriptionsToRenew = subscriptionRepository
                .findByStatusAndEndDateBeforeAndAutoRenewTrue(
                        SubscriptionStatus.ACTIVE,
                        today
                );
        logger.info("Found {} subscriptions to renew", subscriptionsToRenew.size());

        for (Subscription subscription : subscriptionsToRenew) {
            logger.debug("Processing renewal for subscription ID: {}", subscription.getId());
            processRenewal(subscription);
        }
        logger.info("Completed daily subscription renewal process");
    }

    private void processRenewal(Subscription subscription) throws MessagingException {
        logger.info("Processing renewal for subscription ID: {} - Attempt #{}",
                subscription.getId(), subscription.getRenewalAttempts() + 1);

        if (subscription.getRenewalAttempts() >= 4) {
            logger.warn("Maximum renewal attempts reached for subscription ID: {}", subscription.getId());
            cancelSubscription(subscription);
            return;
        }

        SubscriptionPlan planToUse = subscription.getNextSubscriptionPlan() != null ?
                subscription.getNextSubscriptionPlan() :
                subscription.getSubscriptionPlan();
        logger.debug("Using plan ID: {} for renewal", planToUse.getId());

        boolean renewalSuccess = orchestrator.processSubscriptionRenewal(subscription, planToUse);

        if (renewalSuccess) {
            logger.info("Renewal successful for subscription ID: {}", subscription.getId());
            renewSubscription(subscription, planToUse);
        } else {
            logger.warn("Renewal failed for subscription ID: {}", subscription.getId());
            handleFailedRenewal(subscription);
        }

    }

    private void renewSubscription(Subscription subscription, SubscriptionPlan plan) throws MessagingException {
        logger.info("Renewing subscription ID: {} with plan ID: {}", subscription.getId(), plan.getId());

        // Create new subscription period
        Date newStartDate = subscription.getEndDate();
        Date newEndDate = Utils.addPeriod(newStartDate, plan.getPeriod());

        logger.debug("New subscription period: {} to {}", newStartDate, newEndDate);

        subscription.setStartDate(newStartDate);
        subscription.setEndDate(newEndDate);
        subscription.setRenewalAttempts(0);
        subscription.setLastRenewalAttempt(null);
        subscription.setSubscriptionPlan(plan);
        subscription.setNextSubscriptionPlan(null);

        subscriptionRepository.save(subscription);
        logger.info("Successfully updated subscription details for ID: {}", subscription.getId());

        EmailRequest emailRequest = EmailRequest.builder()
                .to("maboukarl2@gmail.com")
                .cc(List.of("yvanos510@gmail.com"))
                .subject("Subscription Renewed")
                .headerText("Subscription Renewed")
                .body(String.format(
                        "<p>The subscription for %s of %s has been successfully renewed. The new end date is %s.</p>",
                        subscription.getService().getName(),
                        subscription.getUser().getUsername(),
                        newEndDate
                ))
                .companyName("MabsPlace")
                .build();

        emailService.sendEmail(emailRequest);
        logger.info("Sent renewal confirmation email for subscription ID: {}", subscription.getId());
    }

    private void handleFailedRenewal(Subscription subscription) throws MessagingException {
        subscription.setRenewalAttempts(subscription.getRenewalAttempts() + 1);
        subscription.setLastRenewalAttempt(new Date());

        if (subscription.getRenewalAttempts() >= 4) {
            cancelSubscription(subscription);
        } else {
            subscriptionRepository.save(subscription);

            EmailRequest emailRequest = EmailRequest.builder()
                    .to("maboukarl2@gmail.com")
                    .cc(List.of("yvanos510@gmail.com"))
                    .subject("Subscription Renewal Failed")
                    .headerText("Subscription Renewal Failed")
                    .body(String.format(
                            "<p>There was an issue renewing the subscription for %s of %s. The subscription will be automatically renewed again in the next 24 hours.</p>",
                            subscription.getService().getName(),
                            subscription.getUser().getUsername()
                    ))
                    .companyName("MabsPlace")
                    .build();

            emailService.sendEmail(emailRequest);
        }
    }

    private void cancelSubscription(Subscription subscription) throws MessagingException {
        logger.info("Cancelling subscription ID: {}", subscription.getId());

        Profile profile = subscription.getProfile();
        if (profile != null) {
            logger.debug("Setting profile ID: {} to INACTIVE", profile.getId());
            profile.setStatus(ProfileStatus.INACTIVE);
            profileRepository.save(profile);
            logger.info("Profile status updated to INACTIVE successfully");
        } else
            logger.debug("No profile associated with subscription ID: {}", subscription.getId());

        logger.debug("Setting subscription status to EXPIRED and turning off auto-renewal");
        subscription.setStatus(SubscriptionStatus.EXPIRED);
        subscription.setAutoRenew(false);
        subscriptionRepository.save(subscription);
        logger.info("Subscription updated successfully with status: {}", SubscriptionStatus.EXPIRED);

        logger.debug("Preparing email notification about expired subscription");
        EmailRequest emailRequest = EmailRequest.builder()
                .to("maboukarl2@gmail.com")
                .cc(List.of("yvanos510@gmail.com"))
                .subject("Subscription Expired")
                .headerText("Subscription Expired")
                .body(String.format(
                        "<p>The subscription of %s has now expired. The Account he was using for %s is %s on the profile %s. You need to change its pin or account password. </p>",
                        subscription.getUser().getUsername(),
                        subscription.getService().getName(),
                        subscription.getProfile().getServiceAccount().getLogin(),
                        subscription.getProfile().getProfileName()
                ))
                .companyName("MabsPlace")
                .build();

        emailService.sendEmail(emailRequest);
        logger.info("Expiration notification email sent for subscription ID: {}", subscription.getId());
    }

    public void updateRenewalPlan(Long subscriptionId, Long newPlanId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", subscriptionId));

        SubscriptionPlan newPlan = subscriptionPlanRepository.findById(newPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", newPlanId));

        subscription.setNextSubscriptionPlan(newPlan);
        subscriptionRepository.save(subscription);
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

        // check if there is already a subscription with the same profile if that subscription is expired or inactive delete it first
        List<Subscription> subscriptions = subscriptionRepository.findByProfileId(newSubscription.getProfile().getId());
        for (Subscription sub : subscriptions) {
            if (sub.getStatus() == SubscriptionStatus.EXPIRED || sub.getStatus() == SubscriptionStatus.INACTIVE) {
                subscriptionRepository.delete(sub);
            }
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
        if (updatedSubscription.getNextSubscriptionPlanId() != 0L)
            updated.setNextSubscriptionPlan(subscriptionPlanRepository.findById(updatedSubscription.getNextSubscriptionPlanId()).orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", updatedSubscription.getNextSubscriptionPlanId())));
        MyService service = myServiceRepository.findById(updatedSubscription.getServiceId()).orElseThrow(() -> new ResourceNotFoundException("Service", "id", updatedSubscription.getServiceId()));

        updated.setService(service);
        updated.setStatus(updatedSubscription.getStatus());
        updated.setEndDate(Utils.addPeriod(updatedSubscription.getStartDate(), updated.getSubscriptionPlan().getPeriod()));


        if (updatedSubscription.getProfileId() != 0L) {

            List<Subscription> subscriptions = subscriptionRepository.findByProfileId(updatedSubscription.getProfileId());
            for (Subscription sub : subscriptions) {
                if (sub.getStatus() == SubscriptionStatus.EXPIRED || sub.getStatus() == SubscriptionStatus.INACTIVE) {
                    subscriptionRepository.delete(sub);
                }
            }

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
        List<Subscription> subscriptions = subscriptionRepository.findByEndDateBeforeAndStatusNotAndAutoRenewFalse(new Date(), SubscriptionStatus.EXPIRED);
        for (Subscription subscription : subscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            Profile profile = subscription.getProfile();
            if (profile != null) {
                profile.setStatus(ProfileStatus.INACTIVE);
                profileRepository.save(profile);
            }

            EmailRequest emailRequest = EmailRequest.builder()
                    .to("maboukarl2@gmail.com")
                    .cc(List.of("yvanos510@gmail.com"))
                    .subject("Subscription Expired")
                    .headerText("Subscription Expired")
                    .body(String.format(
                            "<p>The subscription of %s has now expired. The Account he was using for %s is %s on the profile %s. You need to change its pin or account password. </p>",
                            subscription.getUser().getUsername(),
                            subscription.getService().getName(),
                            subscription.getProfile().getServiceAccount().getLogin(),
                            subscription.getProfile().getProfileName()
                    ))
                    .companyName("MabsPlace")
                    .build();

            emailService.sendEmail(emailRequest);
            subscriptionRepository.save(subscription);
        }
    }

    // Update Subscription Status
    public Subscription updateSubscriptionStatus(Long id, SubscriptionStatus newStatus) {
        Subscription subscription = subscriptionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
        subscription.setStatus(newStatus);
        return subscriptionRepository.save(subscription);
    }

    // Extend Subscription
    public Subscription extendSubscription(Long id, int additionalDays) {
        Subscription subscription = subscriptionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
        subscription.setEndDate(Utils.addDays(subscription.getEndDate(), additionalDays));
        return subscriptionRepository.save(subscription);
    }

    // Find Subscriptions by User
    public List<Subscription> findSubscriptionsByUser(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    // Find Subscriptions by Status
    public List<Subscription> findSubscriptionsByStatus(SubscriptionStatus status) {
        return subscriptionRepository.findByStatus(status);
    }

    // Bulk Update Subscriptions
    public List<Subscription> bulkUpdateSubscriptions(List<Long> subscriptionIds, SubscriptionRequestDto updatedData) {
        List<Subscription> subscriptions = subscriptionRepository.findAllById(subscriptionIds);
        for (Subscription subscription : subscriptions) {
            mapper.partialUpdate(updatedData, subscription);
            subscriptionRepository.save(subscription);
        }
        return subscriptions;
    }

    public List<Subscription> getSubscriptionsByUserId(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }
}
