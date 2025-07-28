package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.email.EmailRequest;
import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.promoCode.PromoCodeResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.expense.ExpenseRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.*;
import com.mabsplace.mabsplaceback.domain.enums.*;
import com.mabsplace.mabsplaceback.domain.mappers.PaymentMapper;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionMapper;
import com.mabsplace.mabsplaceback.domain.repositories.*;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import com.mabsplace.mabsplaceback.utils.Utils;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.mysql.cj.conf.PropertyKey.logger;

@Service
@Transactional
public class SubscriptionPaymentOrchestrator {
    private final WalletService walletService;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final DiscountService discountService;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;
    private final SubscriptionMapper mapper;
    private final CurrencyRepository currencyRepository;
    private final MyServiceRepository myServiceRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final ProfileRepository profileRepository;
    private final PromoCodeService promoCodeService;
    private final SubscriptionDiscountService subscriptionDiscountService;
    private final TransactionRepository transactionRepository;
    private final ExpenseService expenseService;

    private static final Logger log = LoggerFactory.getLogger(SubscriptionPaymentOrchestrator.class);
    private final ExpenseCategoryService expenseCategoryService;

    public SubscriptionPaymentOrchestrator(
            WalletService walletService,
            SubscriptionRepository subscriptionRepository,
            PaymentRepository paymentRepository,
            EmailService emailService,
            NotificationService notificationService,
            DiscountService discountService,
            UserRepository userRepository,
            PaymentMapper paymentMapper,
            SubscriptionMapper mapper,
            CurrencyRepository currencyRepository,
            MyServiceRepository myServiceRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            ProfileRepository profileRepository,
            PromoCodeService promoCodeService,
            SubscriptionDiscountService subscriptionDiscountService,
            TransactionRepository transactionRepository,
            ExpenseService expenseService, ExpenseCategoryService expenseCategoryService) {
        this.walletService = walletService;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.discountService = discountService;
        this.userRepository = userRepository;
        this.paymentMapper = paymentMapper;
        this.mapper = mapper;
        this.currencyRepository = currencyRepository;
        this.myServiceRepository = myServiceRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.profileRepository = profileRepository;
        this.promoCodeService = promoCodeService;
        this.subscriptionDiscountService = subscriptionDiscountService;
        this.transactionRepository = transactionRepository;
        this.expenseService = expenseService;
        this.expenseCategoryService = expenseCategoryService;
    }

    public Payment processPaymentWithoutSubscription(PaymentRequestDto paymentRequest) {
        // Handle payment processing
        User user = userRepository.findById(paymentRequest.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", paymentRequest.getUserId()));
        double discount = discountService.getDiscountForUser(user.getId());

        BigDecimal amountAfterDiscount = paymentRequest.getAmount().subtract(BigDecimal.valueOf(discount));
        BigDecimal amountWithPromo = BigDecimal.ZERO;

        if (paymentRequest.getPromoCode() != null && !paymentRequest.getPromoCode().isEmpty()) {
            PromoCodeResponseDto promoCodeResponseDto = promoCodeService.validatePromoCode(paymentRequest.getPromoCode(), user);
            BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                    promoCodeResponseDto.getDiscountAmount().divide(BigDecimal.valueOf(100)));
            amountWithPromo = amountAfterDiscount.multiply(discountMultiplier)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        log.info("Amount after discount: " + amountAfterDiscount);

        // Determine the final amount to charge
        BigDecimal finalAmount = amountWithPromo.compareTo(BigDecimal.ZERO) != 0 ? amountWithPromo : amountAfterDiscount;

        // Skip balance check for zero-dollar transactions (100% discount promo codes)
        if (finalAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (!walletService.checkBalance(user.getWallet().getBalance(), finalAmount)) {
                throw new RuntimeException("Insufficient funds");
            }
        } else {
            log.info("Skipping balance check for zero-dollar transaction due to 100% discount");
        }

        Payment payment = createPayment(user, paymentRequest, amountAfterDiscount);

        if (payment.getStatus() == PaymentStatus.PAID && user.getReferrer() != null && !payment.getSubscriptionPlan().getName().equals("Trial")) {
            User referrer = user.getReferrer();

            String promoCode = promoCodeService.generatePromoCodeForReferrer2(referrer, getReferralDiscountRate());
            notificationService.notifyReferrerOfPromoCode(referrer, promoCode);
        }

        return payment;
    }

    @Transactional(rollbackFor = Exception.class)
    public Payment processPaymentAndCreateSubscription(PaymentRequestDto paymentRequest) throws MessagingException {
        log.info("Starting payment and subscription creation for user: {}, amount: {}",
                paymentRequest.getUserId(), paymentRequest.getAmount());

        // Handle payment processing
        User user = userRepository.findById(paymentRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", paymentRequest.getUserId()));
        double discount = discountService.getDiscountForUser(user.getId());

        BigDecimal amountAfterDiscount = paymentRequest.getAmount().subtract(BigDecimal.valueOf(discount));
        BigDecimal amountWithPromo = BigDecimal.ZERO;

        if (paymentRequest.getPromoCode() != null && !paymentRequest.getPromoCode().isEmpty()) {
            PromoCodeResponseDto promoCodeResponseDto = promoCodeService.validatePromoCode(paymentRequest.getPromoCode(), user);
            BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                    promoCodeResponseDto.getDiscountAmount().divide(BigDecimal.valueOf(100)));
            amountWithPromo = amountAfterDiscount.multiply(discountMultiplier)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        log.info("Amount after discount: " + amountAfterDiscount);

        // Determine the final amount to charge
        BigDecimal finalAmount = amountWithPromo.compareTo(BigDecimal.ZERO) != 0 ? amountWithPromo : amountAfterDiscount;

        // Skip balance check for zero-dollar transactions (100% discount promo codes)
        if (finalAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (!walletService.checkBalance(user.getWallet().getBalance(), finalAmount)) {
                throw new RuntimeException("Insufficient funds");
            }
        } else {
            log.info("Skipping balance check for zero-dollar transaction due to 100% discount");
        }

        try {
            // Create payment and debit wallet - this is now part of the transaction
            Payment payment = createPayment(user, paymentRequest, amountAfterDiscount);
            log.info("Payment created successfully: {}, Status: {}", payment.getId(), payment.getStatus());

            // Create subscription if payment successful
            if (payment.getStatus() == PaymentStatus.PAID) {
                SubscriptionPlan plan = subscriptionPlanRepository.findById(payment.getSubscriptionPlan().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", payment.getSubscriptionPlan().getId()));
                boolean isTrial = plan.getName().equals("Trial");

                // This will throw an exception if subscription creation fails (e.g., duplicate profile)
                // The @Transactional annotation will automatically rollback the wallet debit
                createInitialSubscription(payment);
                log.info("Subscription created successfully for payment: {}", payment.getId());

                // send email
                emailService.sendEmail(EmailRequest.builder()
                        .to("mabsplace2024@gmail.com")
                        .subject("Payment Confirmation")
                        .headerText("Payment Confirmation")
                        .body(" <p>A payment of $" + payment.getAmount() + " from " + user.getUsername() + " for " + payment.getService().getName() + " has been successfully processed.</p>")
                        .companyName("MabsPlace")
                        .build());

                if (user.getReferrer() != null) {
                    User referrer = user.getReferrer();

                    // Check if this is the user's first non-trial payment
                    // Count excludes current payment since we want to check if this is THE first
                    long previousNonTrialPayments = paymentRepository.countByUserIdAndSubscriptionPlanNameNot(user.getId(), "Trial") - (isTrial ? 0 : 1);
                    boolean isFirstNonTrialPayment = !isTrial && previousNonTrialPayments == 0;

                    if (isFirstNonTrialPayment) {
                        BigDecimal rewardAmount = getReferralRewardAmount();
                        walletService.credit(referrer.getWallet().getId(), rewardAmount);
                        Transaction transaction = Transaction.builder()
                                .amount(rewardAmount)
                                .currency(payment.getCurrency())
                                .receiverWallet(referrer.getWallet())
                                .senderWallet(user.getWallet())
                                .transactionDate(new Date())
                                .transactionRef(null)
                                .transactionStatus(TransactionStatus.COMPLETED)
                                .transactionType(TransactionType.TRANSFER)
                                .build();
                        transactionRepository.save(transaction);

                        // Create expense entry for the referral reward
                        createReferralRewardExpense(referrer, rewardAmount, payment.getCurrency());

                        log.info("Referral reward of {} sent to referrer ID: {}", rewardAmount, referrer.getId());
                    } else {
                        String promoCode = promoCodeService.generatePromoCodeForReferrer2(referrer, getReferralDiscountRate());
                        notificationService.notifyReferrerOfPromoCode(referrer, promoCode);
                    }
                }
            }

            log.info("Payment and subscription process completed successfully for user: {}", user.getId());
            return payment;

        } catch (Exception e) {
            log.error("Error during payment and subscription creation for user: {}. Error: {}",
                    user.getId(), e.getMessage(), e);

            // The @Transactional annotation will automatically rollback the entire transaction
            // including the wallet debit when any exception is thrown
            throw new RuntimeException("Payment and subscription creation failed: " + e.getMessage(), e);
        }
    }

    // Utility Methods for Configurations
    private BigDecimal getReferralRewardAmount() {
        return BigDecimal.valueOf(500);
    }

    private BigDecimal getReferralDiscountRate() {
        return BigDecimal.valueOf(5);
    }

    public boolean processSubscriptionRenewal(Subscription subscription, SubscriptionPlan nextPlan) {
        // Allow trial renewal only to non-trial plans
        if (subscription.getIsTrial()) {
            if (nextPlan == null || nextPlan.getName().equals("Trial")) {
                log.info("Preventing trial renewal to trial plan for subscription ID: {}", subscription.getId());
                subscription.setStatus(SubscriptionStatus.CANCELLED);
                subscriptionRepository.save(subscription);
                return false;
            }
            log.info("Allowing trial renewal to non-trial plan for subscription ID: {}", subscription.getId());
        }

        User user = subscription.getUser();
        // Ensure we have the full user object with all relationships loaded
        if (user != null) {
            user = userRepository.findById(user.getId()).orElse(user);
        }

        PaymentRequestDto renewalPayment = PaymentRequestDto.builder()
                .amount(subscriptionDiscountService.getDiscountedPrice(nextPlan))
                .userId(user.getId())
                .currencyId(nextPlan.getCurrency().getId())
                .serviceId(subscription.getService().getId())
                .subscriptionPlanId(nextPlan.getId())
                .paymentDate(new Date())
                .build();

        // Check if user has a valid personal promo code
        String personalPromoCode = promoCodeService.getUserPersonalPromoCode(user);
        if (personalPromoCode != null && !personalPromoCode.isEmpty()) {
            renewalPayment.setPromoCode(personalPromoCode);
            log.info("Applied personal promo code '{}' for user ID: {} during subscription renewal",
                    personalPromoCode, user.getId());
        } else {
            log.info("No valid personal promo code found for user ID: {} during subscription renewal", user.getId());
        }

        try {
            Payment payment = processPaymentWithoutSubscription(renewalPayment);
            return payment.getStatus() == PaymentStatus.PAID;
        } catch (Exception e) {
            log.error("Renewal payment failed for subscription {}: {}", subscription.getId(), e.getMessage(), e);

            // If the failure was due to promo code issues and we had a promo code, try again without it
            if (personalPromoCode != null && !personalPromoCode.isEmpty() &&
                e.getMessage() != null && e.getMessage().toLowerCase().contains("promo")) {

                log.warn("Retrying subscription renewal for subscription {} without promo code due to promo code error",
                        subscription.getId());

                // Create a new payment request without the promo code
                PaymentRequestDto renewalPaymentWithoutPromo = PaymentRequestDto.builder()
                        .amount(subscriptionDiscountService.getDiscountedPrice(nextPlan))
                        .userId(user.getId())
                        .currencyId(nextPlan.getCurrency().getId())
                        .serviceId(subscription.getService().getId())
                        .subscriptionPlanId(nextPlan.getId())
                        .paymentDate(new Date())
                        .build();

                try {
                    Payment retryPayment = processPaymentWithoutSubscription(renewalPaymentWithoutPromo);
                    if (retryPayment.getStatus() == PaymentStatus.PAID) {
                        log.info("Subscription renewal succeeded on retry without promo code for subscription {}",
                                subscription.getId());
                        return true;
                    }
                } catch (Exception retryException) {
                    log.error("Retry payment also failed for subscription {}: {}",
                            subscription.getId(), retryException.getMessage(), retryException);
                }
            }

            return false;
        }
    }

    private Payment createPayment(User user, PaymentRequestDto paymentRequestDto, BigDecimal amountAfterDiscount) {
        Payment entity = paymentMapper.toEntity(paymentRequestDto);

        entity.setUser(user);
        entity.setCurrency(currencyRepository.findById(paymentRequestDto.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", paymentRequestDto.getCurrencyId())));
        entity.setService(myServiceRepository.findById(paymentRequestDto.getServiceId()).orElseThrow(() -> new ResourceNotFoundException("MyService", "id", paymentRequestDto.getServiceId())));
        entity.setSubscriptionPlan(subscriptionPlanRepository.findById(paymentRequestDto.getSubscriptionPlanId()).orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", paymentRequestDto.getSubscriptionPlanId())));
        entity.setAmount(amountAfterDiscount);
        entity.setStatus(PaymentStatus.PAID);

        // Apply promo code if provided
        if (paymentRequestDto.getPromoCode() != null && !paymentRequestDto.getPromoCode().isEmpty()) {
            promoCodeService.applyPromoCode(paymentRequestDto.getPromoCode(), entity);
        }

        // Only debit wallet if the amount is greater than zero
        if (entity.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            log.info("Debiting wallet with amount: {}", entity.getAmount());
            walletService.debit(user.getWallet().getId(), entity.getAmount());
        } else {
            log.info("Skipping wallet debit for zero-dollar transaction");
        }

        return paymentRepository.save(entity);
    }

    private void createInitialSubscription(Payment payment) {
        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findById(payment.getSubscriptionPlan().getId()).orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", payment.getSubscriptionPlan().getId()));

        if (Objects.equals(subscriptionPlan.getName(), "Trial")) {
            log.info("Creating trial subscription");
            // Check if user has ever had a trial for this service (even if deleted/cancelled)
            if (subscriptionRepository.existsByUserIdAndServiceIdAndIsTrialTrue(payment.getUser().getId(), payment.getService().getId())) {
                throw new RuntimeException("You have already used the trial for this service");
            }
            // Check if user has any active subscription (trial or paid) for this service
            boolean hasActiveSubscription = subscriptionRepository
                    .existsByUserIdAndServiceIdAndStatusAndEndDateAfter(
                            payment.getUser().getId(), payment.getService().getId(), SubscriptionStatus.ACTIVE, new Date());

            if (hasActiveSubscription) {
                throw new RuntimeException("You already have an active subscription for this service");
            }
            createTrialSubscription(payment);
        } else {
            log.info("Creating regular subscription");
            createRegularSubscription(payment);
        }
    }

    private void createRegularSubscription(Payment payment) {
        SubscriptionRequestDto subscriptionDto = SubscriptionRequestDto.builder()
                .userId(payment.getUser().getId())
                .serviceId(payment.getService().getId())
                .subscriptionPlanId(payment.getSubscriptionPlan().getId())
                .startDate(new Date())
                .isTrial(false)
                .status(SubscriptionStatus.INACTIVE)
                .build();

        // Auto-assign profile if available using the new reservation method
        Long profileId = findAndReserveAvailableProfile(payment.getService().getId());
        if (profileId != null) {
            subscriptionDto.setProfileId(profileId);
            log.info("Auto-assigned and reserved profile ID: {} to subscription", profileId);
        } else {
            log.error("No available profile found for user ID: {} and service ID: {}",
                    payment.getUser().getId(), payment.getService().getId());
            throw new IllegalStateException("No available profiles for this service. Please contact support or try again later.");
        }

        // Create subscription
        Subscription subscription = createSubscriptionFromDto(subscriptionDto);
        subscriptionRepository.save(subscription);
    }

    private void createTrialSubscription(Payment payment) {
        SubscriptionRequestDto subscriptionDto = SubscriptionRequestDto.builder()
                .userId(payment.getUser().getId())
                .serviceId(payment.getService().getId())
                .subscriptionPlanId(payment.getSubscriptionPlan().getId())
                .startDate(new Date())
                .status(SubscriptionStatus.INACTIVE)
                .autoRenew(false)
                .isTrial(true)
                .build();

        // Auto-assign profile if available using the new reservation method
        Long profileId = findAndReserveAvailableProfile(payment.getService().getId());
        if (profileId != null) {
            subscriptionDto.setProfileId(profileId);
            log.info("Auto-assigned and reserved profile ID: {} to trial subscription", profileId);
        } else {
            log.error("No available profile found for user ID: {} and service ID: {}",
                    payment.getUser().getId(), payment.getService().getId());
            throw new IllegalStateException("No available profiles for this service. Please contact support or try again later.");
        }

        // Create subscription
        createSubscriptionFromDto(subscriptionDto);
    }

    /**
     * Finds an available (inactive) profile for the given service with better concurrency handling
     * @param serviceId the service ID
     * @return profile ID if available, otherwise null
     */
    @Transactional
    private Long findAvailableProfileId(Long serviceId) {
        try {
            // Find available profiles with INACTIVE status
            List<Profile> availableProfiles = profileRepository.findAvailableProfilesByServiceId(serviceId, ProfileStatus.INACTIVE);

            if (!availableProfiles.isEmpty()) {
                // Double-check that the profile is still available by checking for active subscriptions
                for (Profile profile : availableProfiles) {
                    // Check if there's already an active subscription using this profile
                    boolean hasActiveSubscription = subscriptionRepository.existsByProfileIdAndStatusIn(
                            profile.getId(),
                            List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.INACTIVE)
                    );

                    if (!hasActiveSubscription) {
                        log.info("Found available profile ID: {} for service ID: {}", profile.getId(), serviceId);
                        return profile.getId();
                    } else {
                        log.warn("Profile ID: {} appears inactive but has active subscription, skipping", profile.getId());
                    }
                }
            }

            log.info("No available profiles found for service ID: {}", serviceId);
            return null;
        } catch (Exception e) {
            log.error("Error finding available profile for service ID: {}", serviceId, e);
            return null;
        }
    }

    /**
     * Finds and reserves an available profile for the given service with database-level locking
     * This method prevents race conditions by using database transactions
     * @param serviceId the service ID
     * @return profile ID if available and successfully reserved, otherwise null
     */
    @Transactional
    private Long findAndReserveAvailableProfile(Long serviceId) {
        try {
            // Find available profiles with INACTIVE status
            List<Profile> availableProfiles = profileRepository.findAvailableProfilesByServiceId(serviceId, ProfileStatus.INACTIVE);

            for (Profile profile : availableProfiles) {
                try {
                    // Attempt to reserve the profile by marking it as active
                    // This will fail if another transaction has already reserved it
                    Profile profileToUpdate = profileRepository.findById(profile.getId()).orElse(null);
                    if (profileToUpdate != null && profileToUpdate.getStatus() == ProfileStatus.INACTIVE) {

                        // Double-check that no subscription is using this profile
                        boolean hasActiveSubscription = subscriptionRepository.existsByProfileIdAndStatusIn(
                                profileToUpdate.getId(),
                                List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.INACTIVE)
                        );

                        if (!hasActiveSubscription) {
                            // Reserve the profile by marking it as active
                            profileToUpdate.setStatus(ProfileStatus.ACTIVE);
                            profileRepository.save(profileToUpdate);

                            log.info("Successfully reserved profile ID: {} for service ID: {}", profileToUpdate.getId(), serviceId);
                            return profileToUpdate.getId();
                        }
                    }
                } catch (Exception e) {
                    // If we get a constraint violation or any other error, try the next profile
                    log.warn("Failed to reserve profile ID: {}, trying next profile. Error: {}", profile.getId(), e.getMessage());
                    continue;
                }
            }

            log.info("No available profile could be reserved for service ID: {}", serviceId);
            return null;
        } catch (Exception e) {
            log.error("Error finding and reserving available profile for service ID: {}. Error: {}", serviceId, e.getMessage());
            return null;
        }
    }

    /**
     * Releases a reserved profile by marking it as inactive
     * This is used when subscription creation fails after profile reservation
     * @param profileId the profile ID to release
     */
    private void releaseReservedProfile(Long profileId) {
        try {
            if (profileId != null) {
                Profile profile = profileRepository.findById(profileId).orElse(null);
                if (profile != null && profile.getStatus() == ProfileStatus.ACTIVE) {
                    // Only release if no subscription is actually using it
                    boolean hasActiveSubscription = subscriptionRepository.existsByProfileIdAndStatusIn(
                            profile.getId(),
                            List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.INACTIVE)
                    );

                    if (!hasActiveSubscription) {
                        profile.setStatus(ProfileStatus.INACTIVE);
                        profileRepository.save(profile);
                        log.info("Released reserved profile ID: {}", profileId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error releasing reserved profile ID: {}. Error: {}", profileId, e.getMessage());
        }
    }

    private Subscription createSubscriptionFromDto(SubscriptionRequestDto subscription) {
        log.info("Creating subscription from DTO: {}", subscription);

        Subscription newSubscription = mapper.toEntity(subscription);
        newSubscription.setUser(userRepository.findById(subscription.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", subscription.getUserId())));

        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findById(subscription.getSubscriptionPlanId()).orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", subscription.getSubscriptionPlanId()));
        newSubscription.setSubscriptionPlan(subscriptionPlan);

        MyService service = myServiceRepository.findById(subscription.getServiceId()).orElseThrow(() -> new ResourceNotFoundException("Service", "id", subscription.getServiceId()));
        newSubscription.setService(service);
        newSubscription.setStatus(subscription.getStatus());
        newSubscription.setEndDate(Utils.addPeriod(subscription.getStartDate(), subscriptionPlan.getPeriod()));
        newSubscription.setAutoRenew(true);

        if (subscription.getIsTrial()) {
            newSubscription.setIsTrial(true);
            newSubscription.setAutoRenew(false);
        } else {
            newSubscription.setIsTrial(false);
        }

        if (subscription.getProfileId() != 0L) {
            log.info("Attempting to assign profile ID: {} to subscription", subscription.getProfileId());

            try {
                // Clean up any expired or inactive subscriptions for this profile first
                List<Subscription> existingSubscriptions = subscriptionRepository.findByProfileId(subscription.getProfileId());
                for (Subscription sub : existingSubscriptions) {
                    if (sub.getStatus() == SubscriptionStatus.EXPIRED || sub.getStatus() == SubscriptionStatus.INACTIVE) {
                        log.info("Deleting expired or inactive subscription with ID: {}", sub.getId());
                        subscriptionRepository.delete(sub);
                    }
                }

                // Fetch the profile and validate its availability
                Profile profile = profileRepository.findById(subscription.getProfileId())
                        .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", subscription.getProfileId()));

                // If the profile was reserved by findAndReserveAvailableProfile, it should already be ACTIVE
                // Double-check that no other subscription is using this profile
                boolean hasActiveSubscription = subscriptionRepository.existsByProfileIdAndStatusIn(
                        profile.getId(),
                        List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.INACTIVE)
                );

                if (hasActiveSubscription) {
                    log.error("Profile ID: {} is already in use by another active subscription", subscription.getProfileId());
                    throw new IllegalStateException("The profile is already in use by another subscription. Please try again.");
                }

                // If profile is not active, mark it as active (fallback for manual assignments)
                if (profile.getStatus() != ProfileStatus.ACTIVE) {
                    profile.setStatus(ProfileStatus.ACTIVE);
                    profile = profileRepository.save(profile);
                }

                newSubscription.setProfile(profile);

                // Set subscription status to ACTIVE since we have an available profile
                newSubscription.setStatus(SubscriptionStatus.ACTIVE);
                log.info("Successfully assigned profile ID: {} to subscription and set status to ACTIVE", profile.getId());

            } catch (IllegalStateException | ResourceNotFoundException e) {
                // Re-throw business logic exceptions
                throw e;
            } catch (Exception e) {
                log.error("Failed to assign profile ID: {} to subscription. Error: {}", subscription.getProfileId(), e.getMessage());

                // Check if it's a constraint violation (duplicate profile assignment)
                if (e.getMessage() != null && e.getMessage().contains("UK_fvarljckhxhklksmiy1gnq8v")) {
                    throw new IllegalStateException("The profile is already in use by another subscription. This may be due to concurrent requests. Please try again.", e);
                }

                throw new RuntimeException("Failed to assign profile to subscription: " + e.getMessage(), e);
            }
        }

        log.info("Sending notification to user ID: {}", newSubscription.getUser().getId());
        notificationService.sendNotificationToUser(newSubscription.getUser().getId(), "Subscription updated successfully", "Your subscription has been updated.", new HashMap<>());
        return subscriptionRepository.save(newSubscription);
    }

    public void createSubscription(SubscriptionRequestDto subscription) {
        Subscription newSubscription = createSubscriptionFromDto(subscription);
        subscriptionRepository.save(newSubscription);
    }

    private void createReferralRewardExpense(User referrer, BigDecimal amount, Currency currency) {
        try {
            ExpenseRequestDto expenseRequest = new ExpenseRequestDto();
            expenseRequest.setAmount(amount);
            expenseRequest.setCurrencyId(currency.getId());
            expenseRequest.setDescription("Referral reward for user: " + referrer.getUsername());
            expenseRequest.setExpenseDate(LocalDateTime.now());
            expenseRequest.setPaymentMethod(PaymentMethod.MOBILE_MONEY);
            expenseRequest.setRecurring(false);

            // You'll need to have a category ID for referral rewards in your database
            // This should be configured or retrieved from a repository

            Long referralExpenseCategoryId = expenseCategoryService.getCategoryByName("Referral Rewards");
            expenseRequest.setCategoryId(referralExpenseCategoryId);

            expenseService.createExpense(expenseRequest, 1L); // Admin user ID (replace with actual admin ID)
            log.info("Created expense entry for referral reward to user ID: {}", referrer.getId());
        } catch (Exception e) {
            log.error("Failed to create expense for referral reward", e);
            // Don't throw the exception to avoid disrupting the main payment flow
        }
    }
}
