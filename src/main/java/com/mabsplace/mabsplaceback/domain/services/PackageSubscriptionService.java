package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.*;
import com.mabsplace.mabsplaceback.domain.enums.ProfileStatus;
import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
import com.mabsplace.mabsplaceback.domain.enums.TransactionType;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to handle package subscriptions
 * This service aligns with the flow of regular subscriptions but for service packages
 */
@Service
public class PackageSubscriptionService {
    private static final Logger logger = LoggerFactory.getLogger(PackageSubscriptionService.class);
    private static final int MAX_RENEWAL_ATTEMPTS = 3;
    
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final SubscriptionPaymentOrchestrator orchestrator;
    private final ServicePackageRepository packageRepository;
    private final PackageSubscriptionPlanRepository packagePlanRepository;
    private final ProfileRepository profileRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final WalletService walletService;
    private final PaymentRepository paymentRepository;
    
    public PackageSubscriptionService(
            SubscriptionRepository subscriptionRepository,
            SubscriptionService subscriptionService,
            SubscriptionPaymentOrchestrator orchestrator,
            ServicePackageRepository packageRepository,
            PackageSubscriptionPlanRepository packagePlanRepository,
            ProfileRepository profileRepository,
            NotificationService notificationService,
            EmailService emailService,
            WalletService walletService,
            PaymentRepository paymentRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionService = subscriptionService;
        this.orchestrator = orchestrator;
        this.packageRepository = packageRepository;
        this.packagePlanRepository = packagePlanRepository;
        this.profileRepository = profileRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.walletService = walletService;
        this.paymentRepository = paymentRepository;
    }
    
    /**
     * Process payment for package subscription
     * This is the entry point for creating a package subscription, following the same
     * flow as regular subscriptions - payment first, then subscription creation
     * 
     * @param paymentRequest The payment request containing all necessary details
     * @return Payment object containing payment details
     * @throws ApiException If payment processing fails
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Payment processPackageSubscriptionPayment(PaymentRequestDto paymentRequest) {
        logger.info("Processing payment for package subscription for user ID: {} with package plan ID: {}", 
                paymentRequest.getUserId(), paymentRequest.getSubscriptionPlanId());
        
        try {
            // Validate input
            if (paymentRequest.getServicePackageId() <= 0) {
                throw new ApiException("Service package ID is required");
            }
            
            if (paymentRequest.getSubscriptionPlanId() <= 0) {
                throw new ApiException("Package subscription plan ID is required");
            }
            
            // Check if package exists
            ServicePackage servicePackage = packageRepository.findById(paymentRequest.getServicePackageId())
                    .orElseThrow(() -> new ResourceNotFoundException("ServicePackage", "id", paymentRequest.getServicePackageId()));
            
            // Check if package plan exists
            PackageSubscriptionPlan packagePlan = packagePlanRepository.findById(paymentRequest.getSubscriptionPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("PackageSubscriptionPlan", "id", paymentRequest.getSubscriptionPlanId()));
            
            // Validate that package plan belongs to the package
            if (!packagePlan.getServicePackage().getId().equals(servicePackage.getId())) {
                throw new ApiException("The specified subscription plan does not belong to the specified package");
            }
            
            // Fetch user
            User user = orchestrator.getUserById(paymentRequest.getUserId());
            
            // Create and process the payment
            Payment payment = createPackagePayment(user, paymentRequest, packagePlan);
            
            // If payment is successful, create the subscription
            if (payment.getStatus() == com.mabsplace.mabsplaceback.domain.enums.PaymentStatus.PAID) {
                createPackageSubscriptionFromPayment(payment, packagePlan, servicePackage);
            }
            
            return payment;
        } catch (ResourceNotFoundException | ApiException e) {
            // Let these exceptions propagate
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error processing package subscription payment", e);
            throw new ApiException("An unexpected error occurred during payment processing: " + e.getMessage());
        }
    }
    
    /**
     * Create a payment record for a package subscription
     */
    private Payment createPackagePayment(User user, PaymentRequestDto paymentRequest, PackageSubscriptionPlan packagePlan) {
        // Get any user-specific discount
        double userDiscount = orchestrator.discountService.getDiscountForUser(user.getId());
        BigDecimal originalAmount = packagePlan.getPrice();
        BigDecimal amountAfterDiscount = originalAmount.subtract(BigDecimal.valueOf(userDiscount));
        
        // Apply promo code if provided
        BigDecimal finalAmount = amountAfterDiscount;
        if (StringUtils.hasText(paymentRequest.getPromoCode())) {
            try {
                orchestrator.promoCodeService.validatePromoCode(paymentRequest.getPromoCode(), user);
                BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                        orchestrator.promoCodeService.validatePromoCode(paymentRequest.getPromoCode(), user).getDiscountAmount().divide(BigDecimal.valueOf(100)));
                finalAmount = amountAfterDiscount.multiply(discountMultiplier);
                logger.info("Applied promo code: {}. Final amount: {}", paymentRequest.getPromoCode(), finalAmount);
            } catch (Exception e) {
                logger.warn("Failed to apply promo code: {}", paymentRequest.getPromoCode(), e);
                // Continue with original amount if promo code application fails
            }
        }
        
        // Check if user has sufficient funds
        if (!walletService.checkBalance(user.getWallet().getBalance(), finalAmount)) {
            throw new ApiException("Insufficient funds for package subscription. Required: " + finalAmount);
        }
        
        // Create payment record
        Payment payment = Payment.builder()
                .user(user)
                .amount(finalAmount)
                .currency(packagePlan.getCurrency())
                .paymentDate(new Date())
                .status(com.mabsplace.mabsplaceback.domain.enums.PaymentStatus.PAID)
                .servicePackage(packagePlan.getServicePackage())
                .build();
        
        // If promo code provided, store it with the payment
        if (StringUtils.hasText(paymentRequest.getPromoCode())) {
            try {
                orchestrator.promoCodeService.applyPromoCode(paymentRequest.getPromoCode(), payment);
            } catch (Exception e) {
                logger.warn("Error applying promo code to payment: {}", e.getMessage());
            }
        }
        
        // Save the payment
        payment = paymentRepository.save(payment);
        
        // Process wallet transaction - debit the user's wallet
        walletService.createTransaction(
                user.getWallet().getId(),
                TransactionType.SUBSCRIPTION_PAYMENT,
                finalAmount,
                "Package Subscription: " + packagePlan.getServicePackage().getName()
        );
        
        logger.info("Payment processed successfully for package subscription: {}", payment.getId());
        return payment;
    }
    
    /**
     * Create a package subscription from a successful payment
     */
    private Subscription createPackageSubscriptionFromPayment(Payment payment, PackageSubscriptionPlan packagePlan, ServicePackage servicePackage) {
        logger.info("Creating package subscription from payment ID: {}", payment.getId());
        User user = payment.getUser();
        
        // Check if user already has an active subscription for this package
        List<Subscription> existingSubscriptions = subscriptionRepository.findByUserIdAndServicePackageIdAndStatus(
                user.getId(), servicePackage.getId(), SubscriptionStatus.ACTIVE);
        
        if (!existingSubscriptions.isEmpty()) {
            logger.warn("User {} already has an active subscription for package {}", 
                    user.getId(), servicePackage.getId());
            throw new ApiException("You already have an active subscription for this package");
        }
        
        // Validate service accounts availability
        validateServiceAccountsAvailability(servicePackage);
        
        // Create the subscription record
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPackageSubscriptionPlan(packagePlan);
        subscription.setServicePackage(servicePackage);
        subscription.setStatus(SubscriptionStatus.INACTIVE); // Will be activated after profile setup
        subscription.setAutoRenew(true);
        subscription.setStartDate(new Date());
        subscription.setEndDate(orchestrator.calculateEndDate(packagePlan.getPeriod()));
        subscription.setIsPackageSubscription(true);
        subscription.setRenewalAttempts(0);
        
        // Save the subscription to get an ID
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        
        // Note: Profiles will be activated separately by an admin
        // We don't automatically create profiles here to match the regular subscription flow
        
        // Notify the user of successful subscription creation
        try {
            notificationService.notifyUserOfNewPackageSubscription(
                    user, servicePackage, packagePlan);
        } catch (Exception e) {
            logger.error("Failed to send notification for new package subscription", e);
        }
        
        try {
            emailService.sendPackageSubscriptionConfirmationEmail(user.getEmail(), 
                    servicePackage.getName(), 
                    subscription.getEndDate());
        } catch (MessagingException e) {
            logger.error("Failed to send package subscription confirmation email", e);
        }
        
        logger.info("Package subscription created successfully with ID: {}", savedSubscription.getId());
        return savedSubscription;
    }
    
    /**
     * Validates that all services in a package have available service accounts
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
            throw new ApiException("The following services have no available accounts: " + serviceNames);
        }
    }
    
    /**
     * Activate profiles for a package subscription
     * This method should be called by an admin after the subscription is created
     * 
     * @param subscriptionId The ID of the package subscription
     * @return List of activated profiles
     * @throws ApiException If profile activation fails
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public List<Profile> activateProfilesForPackageSubscription(Long subscriptionId) {
        logger.info("Activating profiles for package subscription ID: {}", subscriptionId);
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", subscriptionId));
        
        if (!subscription.getIsPackageSubscription()) {
            throw new ApiException("Subscription is not a package subscription");
        }
        
        if (subscription.getStatus() != SubscriptionStatus.INACTIVE) {
            throw new ApiException("Only inactive subscriptions can have profiles activated. Current status: " + subscription.getStatus());
        }
        
        User user = subscription.getUser();
        ServicePackage servicePackage = subscription.getServicePackage();
        
        List<Profile> activatedProfiles = activateProfilesForPackage(user, servicePackage);
        
        if (activatedProfiles.isEmpty()) {
            throw new ApiException("Failed to activate any profiles for the package services");
        }
        
        // Set the first profile as the main profile for this subscription
        subscription.setProfile(activatedProfiles.get(0));
        
        // Now activate the subscription
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(subscription);
        
        logger.info("Successfully activated {} profiles and subscription for package subscription {}", 
                activatedProfiles.size(), subscriptionId);
        
        return activatedProfiles;
    }
    
    /**
     * Activate profiles for each service in a package
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
            
            // Notify user
            try {
                notificationService.notifyUserOfCancelledPackageSubscription(user, servicePackage);
            } catch (Exception e) {
                logger.error("Failed to send cancellation notification for package subscription", e);
            }
            
            try {
                emailService.sendPackageSubscriptionCancellationEmail(user.getEmail(), servicePackage.getName());
            } catch (MessagingException e) {
                logger.error("Failed to send package subscription cancellation email", e);
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
     * Renew a package subscription - following the same flow as regular subscriptions
     * 
     * @param subscription The subscription to renew
     * @return true if renewal was successful, false otherwise
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
            
            // Create payment request for renewal
            PaymentRequestDto renewalPayment = PaymentRequestDto.builder()
                    .amount(planToUse.getPrice())
                    .userId(user.getId())
                    .currencyId(planToUse.getCurrency().getId())
                    .servicePackageId(subscription.getServicePackage().getId())
                    .subscriptionPlanId(planToUse.getId())
                    .paymentDate(new Date())
                    .build();
            
            // Process payment
            boolean paymentSuccess = false;
            try {
                Payment payment = orchestrator.processPaymentWithoutSubscription(renewalPayment);
                paymentSuccess = payment.getStatus() == com.mabsplace.mabsplaceback.domain.enums.PaymentStatus.PAID;
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
                subscriptionRepository.save(subscription);
                
                // Notify user
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
                if (newAttempts >= MAX_RENEWAL_ATTEMPTS) {
                    logger.warn("Maximum renewal attempts ({}) reached for subscription {}. Marking as expired.", 
                            MAX_RENEWAL_ATTEMPTS, subscription.getId());
                    subscription.setStatus(SubscriptionStatus.EXPIRED);
                }
                
                subscriptionRepository.save(subscription);
                
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
            
            // Notify user
            try {
                notificationService.notifyUserOfExpiredPackageSubscription(user, servicePackage);
            } catch (Exception e) {
                logger.error("Failed to send expiration notification for package subscription", e);
            }
            
            try {
                emailService.sendPackageSubscriptionExpiredEmail(user.getEmail(), servicePackage.getName());
            } catch (MessagingException e) {
                logger.error("Failed to send package subscription expired email", e);
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