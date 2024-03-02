package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Payment;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.enums.PaymentStatus;
import com.mabsplace.mabsplaceback.domain.mappers.PaymentMapper;
import com.mabsplace.mabsplaceback.domain.repositories.*;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
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
  private final SubscriptionService subscriptionService;

  private final DiscountService discountService;

  public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper, UserRepository userRepository, CurrencyRepository currencyRepository, MyServiceRepository myServiceRepository, SubscriptionPlanRepository subscriptionPlanRepository, WalletService walletService, SubscriptionService subscriptionService, DiscountService discountService) {
    this.paymentRepository = paymentRepository;
    this.paymentMapper = paymentMapper;
    this.userRepository = userRepository;
    this.currencyRepository = currencyRepository;
    this.myServiceRepository = myServiceRepository;
    this.subscriptionPlanRepository = subscriptionPlanRepository;
    this.walletService = walletService;
    this.subscriptionService = subscriptionService;
      this.discountService = discountService;
  }

  public Payment createPayment(PaymentRequestDto paymentRequestDto) throws RuntimeException {
    Payment entity = paymentMapper.toEntity(paymentRequestDto);
    User user = userRepository.findById(paymentRequestDto.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", paymentRequestDto.getUserId()));

    double discount = discountService.getDiscountForUser(user.getId());
    BigDecimal amountAfterDiscount = paymentRequestDto.getAmount().subtract(BigDecimal.valueOf(discount));

    boolean checkBalance = walletService.checkBalance(user.getWallet().getId(), amountAfterDiscount);

    if (!checkBalance) {
      throw new RuntimeException("Insufficient funds");
    }

    entity.setUser(user);
    entity.setCurrency(currencyRepository.findById(paymentRequestDto.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", paymentRequestDto.getCurrencyId())));
    entity.setService(myServiceRepository.findById(paymentRequestDto.getServiceId()).orElseThrow(() -> new ResourceNotFoundException("MyService", "id", paymentRequestDto.getServiceId())));
    entity.setSubscriptionPlan(subscriptionPlanRepository.findById(paymentRequestDto.getSubscriptionPlanId()).orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", paymentRequestDto.getSubscriptionPlanId())));
    entity.setAmount(amountAfterDiscount);
    entity.setStatus(PaymentStatus.PENDING);

    walletService.debit(user.getWallet().getId(), amountAfterDiscount);

    return paymentRepository.save(entity);
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

      subscriptionService.createSubscription(subscription);
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
    return paymentRepository.save(updated);
  }

  public void deletePayment(Long id) {
    paymentRepository.deleteById(id);
  }


}
