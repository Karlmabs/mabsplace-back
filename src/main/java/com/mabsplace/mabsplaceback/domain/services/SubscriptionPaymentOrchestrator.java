package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.*;
import com.mabsplace.mabsplaceback.domain.enums.PaymentStatus;
import com.mabsplace.mabsplaceback.domain.enums.ProfileStatus;
import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
import com.mabsplace.mabsplaceback.domain.mappers.PaymentMapper;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionMapper;
import com.mabsplace.mabsplaceback.domain.repositories.*;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import com.mabsplace.mabsplaceback.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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

    private static final Logger log = LoggerFactory.getLogger(SubscriptionPaymentOrchestrator.class);

    public SubscriptionPaymentOrchestrator(
            WalletService walletService,
            SubscriptionRepository subscriptionRepository,
            PaymentRepository paymentRepository,
            EmailService emailService,
            NotificationService notificationService, DiscountService discountService, UserRepository userRepository, PaymentMapper paymentMapper, SubscriptionMapper mapper, CurrencyRepository currencyRepository, MyServiceRepository myServiceRepository, SubscriptionPlanRepository subscriptionPlanRepository, ProfileRepository profileRepository) {
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
    }

    public Payment processPaymentAndCreateSubscription(PaymentRequestDto paymentRequest) {
        // Handle payment processing
        User user = userRepository.findById(paymentRequest.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", paymentRequest.getUserId()));
        double discount = discountService.getDiscountForUser(user.getId());
        BigDecimal amountAfterDiscount = paymentRequest.getAmount().subtract(BigDecimal.valueOf(discount));

        log.info("Amount after discount: " + amountAfterDiscount);

        if (!walletService.checkBalance(user.getWallet().getBalance(), amountAfterDiscount)) {
            throw new RuntimeException("Insufficient funds");
        }

        Payment payment = createPayment(user, paymentRequest, amountAfterDiscount);

        // Create subscription if payment successful
        if (payment.getStatus() == PaymentStatus.PAID) {
            createInitialSubscription(payment);
        }

        return payment;
    }

    public boolean processSubscriptionRenewal(Subscription subscription, SubscriptionPlan nextPlan) {
        PaymentRequestDto renewalPayment = PaymentRequestDto.builder()
                .amount(nextPlan.getPrice())
                .userId(subscription.getUser().getId())
                .currencyId(nextPlan.getCurrency().getId())
                .serviceId(subscription.getService().getId())
                .subscriptionPlanId(nextPlan.getId())
                .paymentDate(new Date())
                .build();

        try {
            Payment payment = processPaymentAndCreateSubscription(renewalPayment);
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

        log.info("Debiting wallet");

        walletService.debit(user.getWallet().getId(), amountAfterDiscount);

        return paymentRepository.save(entity);
    }

    private void createInitialSubscription(Payment payment) {
        SubscriptionRequestDto subscriptionDto = SubscriptionRequestDto.builder()
                .userId(payment.getUser().getId())
                .serviceId(payment.getService().getId())
                .subscriptionPlanId(payment.getSubscriptionPlan().getId())
                .startDate(new Date())
                .status(SubscriptionStatus.INACTIVE)
                .build();

        // Create subscription
        Subscription subscription = createSubscriptionFromDto(subscriptionDto);
        subscriptionRepository.save(subscription);
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