package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Payment;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.mappers.PaymentMapper;
import com.mabsplace.mabsplaceback.domain.repositories.*;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

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

  public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper, UserRepository userRepository, CurrencyRepository currencyRepository, MyServiceRepository myServiceRepository, SubscriptionPlanRepository subscriptionPlanRepository, WalletService walletService) {
    this.paymentRepository = paymentRepository;
    this.paymentMapper = paymentMapper;
    this.userRepository = userRepository;
    this.currencyRepository = currencyRepository;
    this.myServiceRepository = myServiceRepository;
    this.subscriptionPlanRepository = subscriptionPlanRepository;
    this.walletService = walletService;
  }

  public Payment createPayment(PaymentRequestDto paymentRequestDto) throws RuntimeException {
    Payment entity = paymentMapper.toEntity(paymentRequestDto);
    User user = userRepository.findById(paymentRequestDto.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", paymentRequestDto.getUserId()));
    boolean checkBalance = walletService.checkBalance(user.getWallet().getId(), paymentRequestDto.getAmount());

    if (!checkBalance) {
      throw new RuntimeException("Insufficient funds");
    }

    entity.setUser(user);
    entity.setCurrency(currencyRepository.findById(paymentRequestDto.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", paymentRequestDto.getCurrencyId())));
    entity.setService(myServiceRepository.findById(paymentRequestDto.getServiceId()).orElseThrow(() -> new ResourceNotFoundException("MyService", "id", paymentRequestDto.getServiceId())));
    entity.setSubscriptionPlan(subscriptionPlanRepository.findById(paymentRequestDto.getSubscriptionPlanId()).orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", paymentRequestDto.getSubscriptionPlanId())));
    return paymentRepository.save(entity);
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
