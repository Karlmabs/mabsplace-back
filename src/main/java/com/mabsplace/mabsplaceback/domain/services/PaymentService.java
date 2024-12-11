package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.controllers.TransactionController;
import com.mabsplace.mabsplaceback.domain.dtos.email.EmailRequest;
import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Payment;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.enums.PaymentStatus;
import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
import com.mabsplace.mabsplaceback.domain.mappers.PaymentMapper;
import com.mabsplace.mabsplaceback.domain.repositories.*;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;

    private final MyServiceRepository myServiceRepository;

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    private final WalletService walletService;

    private final DiscountService discountService;

    private final EmailService emailService;

    private final SubscriptionPaymentOrchestrator orchestrator;

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final SubscriptionPaymentOrchestrator subscriptionPaymentOrchestrator;

    public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper, UserRepository userRepository, CurrencyRepository currencyRepository, MyServiceRepository myServiceRepository, SubscriptionPlanRepository subscriptionPlanRepository, WalletService walletService, DiscountService discountService, EmailService emailService, SubscriptionPaymentOrchestrator orchestrator, SubscriptionPaymentOrchestrator subscriptionPaymentOrchestrator) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.userRepository = userRepository;
        this.currencyRepository = currencyRepository;
        this.myServiceRepository = myServiceRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.walletService = walletService;
        this.discountService = discountService;
        this.emailService = emailService;
        this.orchestrator = orchestrator;
        this.subscriptionPaymentOrchestrator = subscriptionPaymentOrchestrator;
    }

    public Payment createPayment(PaymentRequestDto paymentRequestDto) {
        return orchestrator.processPaymentAndCreateSubscription(paymentRequestDto);
    }

    public Payment changePaymentStatus(Long id, PaymentStatus status) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        if (status.equals(PaymentStatus.CANCELLED))
            walletService.credit(payment.getUser().getWallet().getId(), payment.getAmount());
        else if (status.equals(PaymentStatus.PAID)) {
            SubscriptionRequestDto subscription = SubscriptionRequestDto.builder()
                    .userId(payment.getUser().getId())
                    .serviceId(payment.getService().getId())
                    .subscriptionPlanId(payment.getSubscriptionPlan().getId())
                    .startDate(new Date())
                    .build();

            subscriptionPaymentOrchestrator.createSubscription(subscription);
        }
        payment.setStatus(status);
        return paymentRepository.save(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
    }

    public Payment updatePayment(Long id, PaymentRequestDto updatedPayment) throws ResourceNotFoundException {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
        Payment updated = paymentMapper.partialUpdate(updatedPayment, payment);
        updated.setUser(userRepository.findById(updatedPayment.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", updatedPayment.getUserId())));
        updated.setCurrency(currencyRepository.findById(updatedPayment.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", updatedPayment.getCurrencyId())));
        updated.setService(myServiceRepository.findById(updatedPayment.getServiceId()).orElseThrow(() -> new ResourceNotFoundException("MyService", "id", updatedPayment.getServiceId())));
        updated.setSubscriptionPlan(subscriptionPlanRepository.findById(updatedPayment.getSubscriptionPlanId()).orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", updatedPayment.getSubscriptionPlanId())));

        // If payment status is PAID, create a subscription without a profile or account associated
        if (updated.getStatus().equals(PaymentStatus.PAID) && !payment.getStatus().equals(PaymentStatus.PAID)) {
            SubscriptionRequestDto subscriptionDto = SubscriptionRequestDto.builder()
                    .userId(updated.getUser().getId())
                    .serviceId(updated.getService().getId())
                    .subscriptionPlanId(updated.getSubscriptionPlan().getId())
                    .startDate(new Date()) // Assuming immediate start date
                    .profileId(0L) // No profile associated
                    .status(SubscriptionStatus.ACTIVE) // Assuming the subscription should be active
                    .build();

            subscriptionPaymentOrchestrator.createSubscription(subscriptionDto);
        }

        return paymentRepository.save(updated);
    }

    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }


}
