package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.entities.*;
import com.mabsplace.mabsplaceback.domain.enums.ProfileStatus;
import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
import com.mabsplace.mabsplaceback.domain.repositories.*;
import com.mabsplace.mabsplaceback.exceptions.ApiException;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to handle package subscriptions
 * This service delegates to the regular subscription service where possible,
 * but handles the package-specific operations
 */
@Service
public class PackageSubscriptionService {
    private static final Logger logger = LoggerFactory.getLogger(PackageSubscriptionService.class);
    
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final SubscriptionPaymentOrchestrator orchestrator;
    private final ServicePackageRepository packageRepository;
    private final PackageSubscriptionPlanRepository packagePlanRepository;
    private final ProfileRepository profileRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    
    public PackageSubscriptionService(
            SubscriptionRepository subscriptionRepository,
            SubscriptionService subscriptionService,
            SubscriptionPaymentOrchestrator orchestrator,
            ServicePackageRepository packageRepository,
            PackageSubscriptionPlanRepository packagePlanRepository,
            ProfileRepository profileRepository,
            NotificationService notificationService,
            EmailService emailService) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionService = subscriptionService;
        this.orchestrator = orchestrator;
        this.packageRepository = packageRepository;
        this.packagePlanRepository = packagePlanRepository;
        this.profileRepository = profileRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }
    
    /**
     * Create a package subscription for a user
     * This creates a single subscription record for the package,
     * but handles all the necessary profile activations for each service
     * 
     * @param userId ID of the user subscribing to the package
     * @param packagePlanId ID of the package subscription plan
     * @param promoCode Optional promo code for discount (can be null)
     * @return The created subscription
     * @throws ResourceNotFoundException if user or package plan not found
     * @throws ApiException if validation fails or other business rules are violated
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Subscription createPackageSubscription(Long userId, Long packagePlanId, String promoCode) {
        logger.info("Creating package subscription for user ID: {} with package plan ID: {}", userId, packagePlanId);
        
        // Input validation
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }
        
        if (packagePlanId == null || packagePlanId <= 0) {
            throw new IllegalArgumentException("Package plan ID must be a positive number");
        }
        
        try {
            // Fetch package subscription plan
            PackageSubscriptionPlan packagePlan = packagePlanRepository.findById(packagePlanId)
                    .orElseThrow(() -> new ResourceNotFoundException("PackageSubscriptionPlan", "id", packagePlanId));
            
            // Fetch user 
            User user = orchestrator.getUserById(userId);
            
            // Check if user already has an active subscription for this package
            List<Subscription> existingSubscriptions = subscriptionRepository.findByUserIdAndServicePackageIdAndStatus(
                    userId, packagePlan.getServicePackage().getId(), SubscriptionStatus.ACTIVE);
            
            if (!existingSubscriptions.isEmpty()) {
                logger.warn("User {} already has an active subscription for package {}", 
                        userId, packagePlan.getServicePackage().getId());
                throw new RuntimeException("You already have an active subscription for this package");
            }
            
            // Validate service accounts availability before proceeding
            validateServiceAccountsAvailability(packagePlan.getServicePackage());
            
            // Create a single subscription record for the package
            Subscription subscription = new Subscription();
            subscription.setUser(user);
            subscription.setPackageSubscriptionPlan(packagePlan);
            subscription.setServicePackage(packagePlan.getServicePackage());
            subscription.setStatus(SubscriptionStatus.INACTIVE); // Will be activated after payment
            subscription.setAutoRenew(true);
            subscription.setStartDate(new Date());
            subscription.setEndDate(orchestrator.calculateEndDate(packagePlan.getPeriod()));
            subscription.setIsPackageSubscription(true);
            subscription.setRenewalAttempts(0);
            
            // Process payment
            try {
                orchestrator.processPackageSubscriptionPayment(user, packagePlan, subscription, promoCode);
            } catch (Exception e) {
                logger.error("Payment processing failed for package subscription", e);
                throw new RuntimeException("Payment processing failed: " + e.getMessage());
            }
            
            // Activate the subscription
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            
            // Save the subscription first to ensure we have an ID
            Subscription savedSubscription = subscriptionRepository.save(subscription);
            
            // For each service in the package, create or activate a profile
            List<Profile> activatedProfiles;
            try {
                activatedProfiles = activateProfilesForPackage(user, packagePlan.getServicePackage());
                
                if (activatedProfiles.isEmpty()) {
                    throw new RuntimeException("Failed to activate any profiles for the package services");
                }
                
                // Set the first profile as the main profile for this subscription
                // This is a bit of a hack, but it allows us to reuse existing code
                savedSubscription.setProfile(activatedProfiles.get(0));
                savedSubscription = subscriptionRepository.save(savedSubscription);
                
            } catch (Exception e) {
                logger.error("Failed to activate profiles for package subscription", e);
                // The transaction will be rolled back
                throw new RuntimeException("Failed to activate profiles: " + e.getMessage());
            }
            
            // Notify user - outside transaction boundary in case of notification failures
            try {
                notificationService.notifyUserOfNewPackageSubscription(
                        user, packagePlan.getServicePackage(), packagePlan);
            } catch (Exception e) {
                logger.error("Failed to send notification for new package subscription", e);
                // Don't fail the transaction for notification errors
            }
            
            try {
                emailService.sendPackageSubscriptionConfirmationEmail(user.getEmail(), 
                        packagePlan.getServicePackage().getName(), 
                        subscription.getEndDate());
            } catch (MessagingException e) {
                logger.error("Failed to send package subscription confirmation email", e);
                // Don't fail the transaction for email errors
            }
            
            logger.info("Package subscription created successfully with ID: {}", savedSubscription.getId());
            return savedSubscription;
        } catch (RuntimeException e) {
            // Let these exceptions propagate as is
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error when creating package subscription", e);
            throw new RuntimeException("An unexpected error occurred: " + e.getMessage());
        }
    }
    
    /**
     * Validates that all services in a package have available service accounts
     * 
     * @param servicePackage The package to validate
     * @throws RuntimeException if any service lacks available accounts
     */
    private void validateServiceAccountsAvailability(ServicePackage servicePackage) {
        List<String> unavailableServices = new ArrayList<>();
        
        for (MyService service : servicePackage.getServices()) {
            try {
                // This will throw an exception if no account is available
                orchestrator.getAvailableServiceAccount(service.getId());
            } catch (Exception e) {
                unavailableServices.add(service.getName());
            }
        }
        
        if (!unavailableServices.isEmpty()) {
            String serviceNames = String.join(", ", unavailableServices);
            throw new RuntimeException("The following services have no available accounts: " + serviceNames);
        }
    }
    
    /**
     * Activate profiles for each service in a package
     * 
     * @param user The user who is subscribing to the package
     * @param servicePackage The package containing services to activate profiles for
     * @return List of activated profiles for the services in the package
     * @throws RuntimeException if profile activation fails for any service
     */
    private List<Profile> activateProfilesForPackage(User user, ServicePackage servicePackage) {
        logger.info("Activating profiles for user {} for package {}", user.getId(), servicePackage.getId());
        List<Profile> activatedProfiles = new ArrayList<>();
        List<String> failedServices = new ArrayList<>();
        
        for (MyService service : servicePackage.getServices()) {
            try {
                // Look for existing profiles for this service and user
                List<Profile> existingProfiles = profileRepository.findByUserAndService(user, service);
                
                if (existingProfiles.isEmpty()) {
                    // Create a new profile for this service
                    ServiceAccount account;
                    try {
                        account = orchestrator.getAvailableServiceAccount(service.getId());
                    } catch (Exception e) {
                        logger.error("Failed to get available service account for service {}", service.getId(), e);
                        failedServices.add(service.getName());
                        continue;
                    }
                    
                    Profile profile = Profile.builder()
                            .user(user)
                            .service(service)
                            .account(account)
                            .profileName(user.getUsername() + "_" + service.getName())
                            .status(ProfileStatus.ACTIVE)
                            .build();
                    
                    try {
                        Profile savedProfile = profileRepository.save(profile);
                        activatedProfiles.add(savedProfile);
                        logger.info("Created new profile ID {} for service {} in package subscription", 
                                savedProfile.getId(), service.getName());
                    } catch (Exception e) {
                        logger.error("Failed to save new profile for service {}", service.getId(), e);
                        failedServices.add(service.getName());
                    }
                } else {
                    // Check if any profile is already active
                    boolean hasActiveProfile = existingProfiles.stream()
                            .anyMatch(p -> p.getStatus() == ProfileStatus.ACTIVE);
                    
                    if (hasActiveProfile) {
                        // Use the first active profile
                        Profile activeProfile = existingProfiles.stream()
                                .filter(p -> p.getStatus() == ProfileStatus.ACTIVE)
                                .findFirst()
                                .get();
                        
                        activatedProfiles.add(activeProfile);
                        logger.info("Using already active profile ID {} for service {} in package subscription", 
                                activeProfile.getId(), service.getName());
                    } else {
                        // Activate the first inactive profile
                        Profile profile = existingProfiles.get(0);
                        profile.setStatus(ProfileStatus.ACTIVE);
                        
                        try {
                            Profile savedProfile = profileRepository.save(profile);
                            activatedProfiles.add(savedProfile);
                            logger.info("Activated existing profile ID {} for service {} in package subscription", 
                                    savedProfile.getId(), service.getName());
                        } catch (Exception e) {
                            logger.error("Failed to activate existing profile ID {} for service {}", 
                                    profile.getId(), service.getId(), e);
                            failedServices.add(service.getName());
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Unexpected error activating profile for service {}", service.getId(), e);
                failedServices.add(service.getName());
            }
        }
        
        // If we failed to activate all services, throw exception
        if (activatedProfiles.isEmpty() && !servicePackage.getServices().isEmpty()) {
            throw new ApiException("Failed to activate any profiles for services in the package");
        }
        
        // If we have some failures but some successes, log warning
        if (!failedServices.isEmpty()) {
            String failedServiceNames = String.join(", ", failedServices);
            logger.warn("Failed to activate profiles for services: {} in package {}", 
                    failedServiceNames, servicePackage.getId());
        }
        
        logger.info("Successfully activated {} out of {} profiles for package subscription", 
                activatedProfiles.size(), servicePackage.getServices().size());
        return activatedProfiles;
    }
    
    /**
     * Cancel a package subscription
     * 
     * @param subscriptionId ID of the subscription to cancel
     * @throws ResourceNotFoundException if subscription not found
     * @throws ApiException if validation fails or cancellation fails
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void cancelPackageSubscription(Long subscriptionId) {
        logger.info("Cancelling package subscription with ID: {}", subscriptionId);
        
        if (subscriptionId == null || subscriptionId <= 0) {
            throw new IllegalArgumentException("Subscription ID must be a positive number");
        }
        
        try {
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", subscriptionId));
            
            if (!subscription.getIsPackageSubscription()) {
                throw new IllegalArgumentException("Subscription is not a package subscription");
            }
            
            // Only allow cancellation if subscription is active
            if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
                throw new ApiException("Only active subscriptions can be cancelled. Current status: " + subscription.getStatus());
            }
            
            ServicePackage servicePackage = subscription.getServicePackage();
            User user = subscription.getUser();
            
            logger.info("Deactivating profiles for all services in package with ID: {}", servicePackage.getId());
            
            // Deactivate profiles for all services in the package - using batch processing
            // First, collect all profiles that need to be deactivated
            List<Profile> profilesToDeactivate = new ArrayList<>();
            
            for (MyService service : servicePackage.getServices()) {
                List<Profile> profiles = profileRepository.findByUserAndService(user, service);
                for (Profile profile : profiles) {
                    if (profile.getStatus() == ProfileStatus.ACTIVE) {
                        profile.setStatus(ProfileStatus.INACTIVE);
                        profilesToDeactivate.add(profile);
                    }
                }
            }
            
            // Save all profiles in a batch
            if (!profilesToDeactivate.isEmpty()) {
                logger.info("Deactivating {} profiles for package subscription {}", 
                        profilesToDeactivate.size(), subscriptionId);
                profileRepository.saveAll(profilesToDeactivate);
            } else {
                logger.warn("No active profiles found to deactivate for package subscription {}", subscriptionId);
            }
            
            // Cancel the subscription
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscription.setAutoRenew(false);
            subscription.setCancellationDate(new Date());
            subscriptionRepository.save(subscription);
            logger.info("Subscription status updated to CANCELLED for ID: {}", subscriptionId);
            
            // Notify user - outside transaction boundary
            try {
                notificationService.notifyUserOfCancelledPackageSubscription(user, servicePackage);
            } catch (Exception e) {
                logger.error("Failed to send cancellation notification for package subscription", e);
                // Don't fail the transaction for notification errors
            }
            
            try {
                emailService.sendPackageSubscriptionCancellationEmail(user.getEmail(), servicePackage.getName());
            } catch (MessagingException e) {
                logger.error("Failed to send package subscription cancellation email", e);
                // Don't fail the transaction for email errors
            }
            
            logger.info("Package subscription cancelled successfully with ID: {}", subscriptionId);
        } catch (ResourceNotFoundException | IllegalArgumentException | ApiException e) {
            // Let these exceptions propagate as is
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error when cancelling package subscription", e);
            throw new ApiException("An unexpected error occurred during cancellation: " + e.getMessage());
        }
    }
    
    /**
     * Renew a package subscription
     * 
     * @param subscription The subscription to renew
     * @return true if renewal was successful, false otherwise
     * @throws IllegalArgumentException if the subscription is not a package subscription
     * @throws ApiException if validation fails or renewal fails unexpectedly
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public boolean renewPackageSubscription(Subscription subscription) {
        if (subscription == null) {
            throw new IllegalArgumentException("Subscription cannot be null");
        }
        
        logger.info("Renewing package subscription with ID: {}", subscription.getId());
        
        if (!subscription.getIsPackageSubscription()) {
            throw new IllegalArgumentException("Subscription is not a package subscription");
        }
        
        // Validate subscription status for renewal
        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            logger.warn("Attempted to renew cancelled subscription with ID: {}", subscription.getId());
            return false;
        }
        
        try {
            User user = subscription.getUser();
            PackageSubscriptionPlan planToUse = subscription.getNextPackageSubscriptionPlan() != null ?
                    subscription.getNextPackageSubscriptionPlan() :
                    subscription.getPackageSubscriptionPlan();
            
            // Verify that the plan exists
            if (planToUse == null) {
                throw new ApiException("No subscription plan found for renewal");
            }
            
            // Validate that the package still exists and has services
            ServicePackage servicePackage = subscription.getServicePackage();
            if (servicePackage == null || servicePackage.getServices() == null || servicePackage.getServices().isEmpty()) {
                throw new ApiException("Service package is invalid or has no services");
            }
            
            logger.info("Using plan {} for package subscription renewal", planToUse.getId());
            
            // Process payment with retry logic
            boolean paymentSuccess = false;
            try {
                paymentSuccess = orchestrator.processPackageSubscriptionRenewal(subscription, planToUse);
            } catch (Exception e) {
                logger.error("Error processing payment for subscription renewal", e);
                paymentSuccess = false;
            }
            
            if (paymentSuccess) {
                logger.info("Payment successful for renewal of subscription {}", subscription.getId());
                
                // Update subscription end date
                Date newEndDate = orchestrator.calculateEndDate(planToUse.getPeriod());
                subscription.setEndDate(newEndDate);
                
                // Reset renewal attempts
                subscription.setRenewalAttempts(0);
                subscription.setLastRenewalAttempt(new Date());
                
                // If status is not ACTIVE, set it to ACTIVE
                if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
                    subscription.setStatus(SubscriptionStatus.ACTIVE);
                }
                
                // Update subscription plan if next plan exists
                if (subscription.getNextPackageSubscriptionPlan() != null) {
                    logger.info("Updating subscription plan from {} to {}", 
                            subscription.getPackageSubscriptionPlan().getId(),
                            subscription.getNextPackageSubscriptionPlan().getId());
                    subscription.setPackageSubscriptionPlan(subscription.getNextPackageSubscriptionPlan());
                    subscription.setNextPackageSubscriptionPlan(null);
                }
                
                // Save changes to subscription
                try {
                    subscriptionRepository.save(subscription);
                } catch (Exception e) {
                    logger.error("Failed to update subscription after renewal", e);
                    throw new ApiException("Failed to update subscription: " + e.getMessage());
                }
                
                // Ensure all profiles are active - but don't fail renewal if profile activation fails
                try {
                    List<Profile> activatedProfiles = activateProfilesForPackage(user, subscription.getServicePackage());
                    logger.info("Activated {} profiles for package subscription renewal", activatedProfiles.size());
                } catch (Exception e) {
                    logger.error("Failed to activate all profiles during subscription renewal", e);
                    // Continue with renewal despite profile activation issues
                }
                
                // Notify user (outside transaction boundary)
                try {
                    notificationService.notifyUserOfRenewedPackageSubscription(
                            user, subscription.getServicePackage(), planToUse);
                } catch (Exception e) {
                    logger.error("Failed to send notification for renewed package subscription", e);
                }
                
                try {
                    emailService.sendPackageSubscriptionRenewalEmail(user.getEmail(), 
                            subscription.getServicePackage().getName(), newEndDate);
                } catch (MessagingException e) {
                    logger.error("Failed to send package subscription renewal email", e);
                }
                
                logger.info("Package subscription renewed successfully with ID: {}", subscription.getId());
                return true;
            } else {
                logger.warn("Payment failed for renewal of subscription {}", subscription.getId());
                
                // Increment renewal attempts
                int newAttempts = subscription.getRenewalAttempts() + 1;
                subscription.setRenewalAttempts(newAttempts);
                subscription.setLastRenewalAttempt(new Date());
                
                // If too many renewal attempts, mark as expired
                final int MAX_RENEWAL_ATTEMPTS = 3;
                if (newAttempts >= MAX_RENEWAL_ATTEMPTS) {
                    logger.warn("Maximum renewal attempts ({}) reached for subscription {}. Marking as expired.", 
                            MAX_RENEWAL_ATTEMPTS, subscription.getId());
                    subscription.setStatus(SubscriptionStatus.EXPIRED);
                }
                
                subscriptionRepository.save(subscription);
                
                logger.warn("Package subscription renewal failed for ID: {}. Attempt: {}", 
                        subscription.getId(), subscription.getRenewalAttempts());
                
                try {
                    emailService.sendPackageSubscriptionRenewalFailedEmail(user.getEmail(), 
                            subscription.getServicePackage().getName(), subscription.getRenewalAttempts());
                } catch (MessagingException e) {
                    logger.error("Failed to send package subscription renewal failed email", e);
                }
                
                return false;
            }
        } catch (ApiException e) {
            // Let ApiExceptions propagate
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during package subscription renewal", e);
            
            // Record the renewal attempt even if unexpected error occurs
            try {
                subscription.setRenewalAttempts(subscription.getRenewalAttempts() + 1);
                subscription.setLastRenewalAttempt(new Date());
                subscriptionRepository.save(subscription);
            } catch (Exception ex) {
                logger.error("Failed to update renewal attempts after error", ex);
            }
            
            throw new ApiException("Unexpected error during renewal: " + e.getMessage());
        }
    }
    
    /**
     * Handle expiration of package subscriptions
     * 
     * @param subscription The subscription to mark as expired
     * @throws IllegalArgumentException if the subscription is not a package subscription
     * @throws ApiException if validation fails or expiration process fails unexpectedly
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void handleExpiredPackageSubscription(Subscription subscription) {
        if (subscription == null) {
            throw new IllegalArgumentException("Subscription cannot be null");
        }
        
        logger.info("Handling expired package subscription with ID: {}", subscription.getId());
        
        if (!subscription.getIsPackageSubscription()) {
            throw new IllegalArgumentException("Subscription is not a package subscription");
        }
        
        try {
            // Only process if not already expired
            if (subscription.getStatus() == SubscriptionStatus.EXPIRED) {
                logger.info("Subscription {} is already expired. Skipping expiration handling.", subscription.getId());
                return;
            }
            
            User user = subscription.getUser();
            ServicePackage servicePackage = subscription.getServicePackage();
            
            if (servicePackage == null) {
                throw new ApiException("Service package not found for subscription");
            }
            
            logger.info("Deactivating profiles for services in expired package subscription {}", subscription.getId());
            
            // Batch deactivate all profiles
            List<Profile> profilesToDeactivate = new ArrayList<>();
            
            for (MyService service : servicePackage.getServices()) {
                List<Profile> profiles = profileRepository.findByUserAndService(user, service);
                for (Profile profile : profiles) {
                    if (profile.getStatus() == ProfileStatus.ACTIVE) {
                        profile.setStatus(ProfileStatus.INACTIVE);
                        profilesToDeactivate.add(profile);
                    }
                }
            }
            
            // Save all deactivated profiles in a batch
            if (!profilesToDeactivate.isEmpty()) {
                logger.info("Deactivating {} profiles for expired package subscription {}", 
                        profilesToDeactivate.size(), subscription.getId());
                profileRepository.saveAll(profilesToDeactivate);
            } else {
                logger.warn("No active profiles found to deactivate for expired package subscription {}", subscription.getId());
            }
            
            // Mark subscription as expired
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscription.setExpirationDate(new Date());
            subscription.setAutoRenew(false); // Ensure auto-renew is turned off
            subscriptionRepository.save(subscription);
            logger.info("Subscription status updated to EXPIRED for ID: {}", subscription.getId());
            
            // Notify user - outside transaction boundary
            try {
                notificationService.notifyUserOfExpiredPackageSubscription(user, servicePackage);
            } catch (Exception e) {
                logger.error("Failed to send expiration notification for package subscription", e);
                // Don't fail the transaction for notification errors
            }
            
            try {
                emailService.sendPackageSubscriptionExpiredEmail(user.getEmail(), servicePackage.getName());
            } catch (MessagingException e) {
                logger.error("Failed to send package subscription expired email", e);
                // Don't fail the transaction for email errors
            }
            
            logger.info("Expired package subscription handled successfully with ID: {}", subscription.getId());
        } catch (ApiException e) {
            // Let these exceptions propagate
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error handling expired package subscription", e);
            throw new ApiException("An unexpected error occurred when handling expired subscription: " + e.getMessage());
        }
    }
}