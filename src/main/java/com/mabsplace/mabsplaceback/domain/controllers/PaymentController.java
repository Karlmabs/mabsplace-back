package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Payment;
import com.mabsplace.mabsplaceback.domain.enums.PaymentStatus;
import com.mabsplace.mabsplaceback.domain.mappers.PaymentMapper;
import com.mabsplace.mabsplaceback.domain.services.PaymentService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

  private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

  private final PaymentService paymentService;
  private final PaymentMapper mapper;

  public PaymentController(PaymentService paymentService, PaymentMapper paymentMapper) {
    this.paymentService = paymentService;
    this.mapper = paymentMapper;
  }

  @PostMapping
  public ResponseEntity<PaymentResponseDto> createPayment(@RequestBody PaymentRequestDto paymentRequestDto) throws MessagingException {
    logger.info("Creating payment with request: {}", paymentRequestDto);
    Payment payment = paymentService.createPayment(paymentRequestDto);
    logger.info("Created payment: {}", payment);
    return new ResponseEntity<>(mapper.toDto(payment), HttpStatus.CREATED);
  }

  // get all payments of a user
  @GetMapping("/user/{userId}")
  public ResponseEntity<List<PaymentResponseDto>> getPaymentsByUserId(@PathVariable Long userId)
    {
      logger.info("Fetching payments for user ID: {}", userId);
      List<Payment> payments = paymentService.getPaymentsByUserId(userId);
      logger.info("Fetched {} payments for user ID: {}", payments.size(), userId);
      return ResponseEntity.ok(mapper.toDtoList(payments));
    }

  @GetMapping("/{id}")
  public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable Long id) {
    logger.info("Fetching payment with ID: {}", id);
    Payment payment = paymentService.getPaymentById(id);
    logger.info("Fetched payment: {}", payment);
    return ResponseEntity.ok(mapper.toDto(payment));
  }

  @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'GET_PAYMENTS')")
  @GetMapping
  public ResponseEntity<List<PaymentResponseDto>> getAllPayments() {
    logger.info("Fetching all payments");
    List<Payment> payments = paymentService.getAllPayments();
    logger.info("Fetched {} payments", payments.size());
    return ResponseEntity.ok(mapper.toDtoList(payments));
  }

  @PutMapping("/{id}")
  public ResponseEntity<PaymentResponseDto> updatePayment(@PathVariable Long id, @RequestBody PaymentRequestDto updatedPayment) {
    logger.info("Updating payment with ID: {}, Request: {}", id, updatedPayment);
    Payment payment = paymentService.updatePayment(id, updatedPayment);
    logger.info("Updated payment: {}", payment);
    return ResponseEntity.ok(mapper.toDto(payment));
  }

  @PatchMapping("/{id}/{status}")
  public ResponseEntity<PaymentResponseDto> updatePaymentStatus(@PathVariable("id") Long id, @PathVariable("status") String status) {
    logger.info("Updating payment status for ID: {} to status: {}", id, status);
    Payment payment = paymentService.changePaymentStatus(id, PaymentStatus.valueOf(status));
    logger.info("Updated payment status successfully: {}", payment);
    return ResponseEntity.ok(mapper.toDto(payment));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
    logger.info("Deleting payment with ID: {}", id);
    paymentService.deletePayment(id);
    logger.info("Deleted payment successfully with ID: {}", id);
    return ResponseEntity.noContent().build();
  }
}
