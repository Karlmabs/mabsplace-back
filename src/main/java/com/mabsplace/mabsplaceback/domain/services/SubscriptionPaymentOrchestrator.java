package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.promoCode.PromoCodeResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.*;
import com.mabsplace.mabsplaceback.domain.enums.*;
import com.mabsplace.mabsplaceback.domain.mappers.PaymentMapper;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionMapper;
import com.mabsplace.mabsplaceback.domain.repositories.*;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import com.mabsplace.mabsplaceback.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.mysql.cj.conf.PropertyKey.logger;

@Service
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

    private static final Logger log = LoggerFactory.getLogger(SubscriptionPaymentOrchestrator.class);

    public SubscriptionPaymentOrchestrator(
            WalletService walletService,
            SubscriptionRepository subscriptionRepository,
            PaymentRepository paymentRepository,
            EmailService emailService,
            NotificationService notificationService, DiscountService discountService, UserRepository userRepository, PaymentMapper paymentMapper, SubscriptionMapper mapper, CurrencyRepository currencyRepository, MyServiceRepository myServiceRepository, SubscriptionPlanRepository subscriptionPlanRepository, ProfileRepository profileRepository, PromoCodeService promoCodeService, SubscriptionDiscountService subscriptionDiscountService, TransactionRepository transactionRepository) {
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

        if (!walletService.checkBalance(user.getWallet().getBalance(), amountWithPromo.compareTo(BigDecimal.ZERO) != 0 ? amountWithPromo : amountAfterDiscount)) {
            throw new RuntimeException("Insufficient funds");
        }

        Payment payment = createPayment(user, paymentRequest, amountAfterDiscount);

        if (payment.getStatus() == PaymentStatus.PAID && user.getReferrer() != null) {
            User referrer = user.getReferrer();

            String promoCode = promoCodeService.generatePromoCodeForReferrer2(referrer, getReferralDiscountRate());
            notificationService.notifyReferrerOfPromoCode(referrer, promoCode);
        }

        return payment;
    }

    public Payment processPaymentAndCreateSubscription(PaymentRequestDto paymentRequest) {
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

        if (!walletService.checkBalance(user.getWallet().getBalance(), amountWithPromo.compareTo(BigDecimal.ZERO) != 0 ? amountWithPromo : amountAfterDiscount)) {
            throw new RuntimeException("Insufficient funds");
        }

        Payment payment = createPayment(user, paymentRequest, amountAfterDiscount);

        // Create subscription if payment successful
        if (payment.getStatus() == PaymentStatus.PAID) {
            createInitialSubscription(payment);

            if (user.getReferrer() != null) {
                User referrer = user.getReferrer();

                // Check if this is the user's first payment
                boolean isFirstPayment = paymentRepository.countByUserId(user.getId()) == 1;

                if (isFirstPayment) {
                    BigDecimal rewardAmount = getReferralRewardAmount(); // Fetch reward configuration
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
                } else {
                    String promoCode = promoCodeService.generatePromoCodeForReferrer2(referrer, getReferralDiscountRate());
                    notificationService.notifyReferrerOfPromoCode(referrer, promoCode);
                }
            }
        }

        return payment;
    }

    // Utility Methods for Configurations
    private BigDecimal getReferralRewardAmount() {
        return BigDecimal.valueOf(500);
    }

    private BigDecimal getReferralDiscountRate() {
        return BigDecimal.valueOf(5);
    }

    public boolean processSubscriptionRenewal(Subscription subscription, SubscriptionPlan nextPlan) {
        User user = subscription.getUser();

        PaymentRequestDto renewalPayment = PaymentRequestDto.builder()
                .amount(subscriptionDiscountService.getDiscountedPrice(nextPlan))
                .userId(subscription.getUser().getId())
                .currencyId(nextPlan.getCurrency().getId())
                .serviceId(subscription.getService().getId())
                .subscriptionPlanId(nextPlan.getId())
                .paymentDate(new Date())
                .build();

        // Check if user has a personal promo code
        String personalPromoCode = promoCodeService.getUserPersonalPromoCode(user);
        if (personalPromoCode != null && !personalPromoCode.isEmpty()) {
            renewalPayment.setPromoCode(personalPromoCode);
            log.info("Applied personal promo code '{}' for user ID: {}", personalPromoCode, user.getId());
        }

        try {
            Payment payment = processPaymentWithoutSubscription(renewalPayment);
            return payment.getStatus() == PaymentStatus.PAID;
        } catch (Exception e) {
            log.error("Renewal payment failed for subscription {}", subscription.getId(), e);
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

        log.info("Debiting wallet");

        walletService.debit(user.getWallet().getId(), entity.getAmount());

        return paymentRepository.save(entity);
    }

    private void createInitialSubscription(Payment payment) {
        SubscriptionPlan subscriptionPlan = subscriptionPlanRepository.findById(payment.getSubscriptionPlan().getId()).orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", payment.getSubscriptionPlan().getId()));

        if (Objects.equals(subscriptionPlan.getName(), "Trial")) {
            log.info("Creating trial subscription");
            if (subscriptionRepository.existsByUserIdAndServiceIdAndIsTrial(payment.getUser().getId(), payment.getService().getId(), true)) {
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

        // Create subscription
        createSubscriptionFromDto(subscriptionDto);
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

        if (subscription.getIsTrial()) {
            newSubscription.setIsTrial(true);
        } else {
            newSubscription.setIsTrial(false);
        }

        if (subscription.getProfileId() != 0L) {
            log.info("Checking existing subscriptions for profile ID: {}", subscription.getProfileId());
            List<Subscription> subscriptions = subscriptionRepository.findByProfileId(subscription.getProfileId());
            for (Subscription sub : subscriptions) {
                if (sub.getStatus() == SubscriptionStatus.EXPIRED || sub.getStatus() == SubscriptionStatus.INACTIVE) {
                    log.info("Deleting expired or inactive subscription with ID: {}", sub.getId());
                    subscriptionRepository.delete(sub);
                }
            }

            Profile profile = profileRepository.findById(subscription.getProfileId()).orElseThrow(() -> new ResourceNotFoundException("Profile", "id", subscription.getProfileId()));

            if (profile.getStatus() == ProfileStatus.ACTIVE) {
                log.error("Profile with ID: {} is already active", subscription.getProfileId());
                throw new IllegalStateException("The profile is already active and cannot be used for a new subscription.");
            }

            profile.setStatus(ProfileStatus.ACTIVE);
            profile = profileRepository.save(profile);
            newSubscription.setProfile(profile);
        }

        log.info("Sending notification to user ID: {}", newSubscription.getUser().getId());
        notificationService.sendNotificationToUser(newSubscription.getUser().getId(), "Subscription updated successfully", "Your subscription has been updated.", new HashMap<>());
        return subscriptionRepository.save(newSubscription);
    }

    public void createSubscription(SubscriptionRequestDto subscription) {
        Subscription newSubscription = createSubscriptionFromDto(subscription);
        subscriptionRepository.save(newSubscription);
    }
}