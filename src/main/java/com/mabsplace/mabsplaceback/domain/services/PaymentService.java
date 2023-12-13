package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.Payment;
import com.mabsplace.mabsplaceback.domain.mappers.PaymentMapper;
import com.mabsplace.mabsplaceback.domain.repositories.CurrencyRepository;
import com.mabsplace.mabsplaceback.domain.repositories.PaymentRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentMapper paymentMapper;

  private final UserRepository userRepository;
  private final CurrencyRepository currencyRepository;

  public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper, UserRepository userRepository, CurrencyRepository currencyRepository) {
    this.paymentRepository = paymentRepository;
    this.paymentMapper = paymentMapper;
    this.userRepository = userRepository;
    this.currencyRepository = currencyRepository;
  }

  public Payment createPayment(PaymentRequestDto paymentRequestDto) throws ResourceNotFoundException {
    Payment entity = paymentMapper.toEntity(paymentRequestDto);
    entity.setUser(userRepository.findById(paymentRequestDto.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", paymentRequestDto.getUserId())));
    entity.setCurrency(currencyRepository.findById(paymentRequestDto.getCurrencyId()).orElseThrow(() -> new ResourceNotFoundException("Currency", "id", paymentRequestDto.getCurrencyId())));
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
    return paymentRepository.save(updated);
  }

  public void deletePayment(Long id) {
    paymentRepository.deleteById(id);
  }
}
