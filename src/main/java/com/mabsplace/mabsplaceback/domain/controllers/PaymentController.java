package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Payment;
import com.mabsplace.mabsplaceback.domain.enums.PaymentStatus;
import com.mabsplace.mabsplaceback.domain.mappers.PaymentMapper;
import com.mabsplace.mabsplaceback.domain.services.PaymentService;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

  private final PaymentService paymentService;
  private final PaymentMapper mapper;

  public PaymentController(PaymentService paymentService, PaymentMapper paymentMapper) {
    this.paymentService = paymentService;
    this.mapper = paymentMapper;
  }

  @PostMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<PaymentResponseDto> createPayment(@RequestBody PaymentRequestDto paymentRequestDto) throws MessagingException {
    Payment createdPayment = paymentService.createPayment(paymentRequestDto);
    return new ResponseEntity<>(mapper.toDto(createdPayment), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable Long id) {
    return ResponseEntity.ok(mapper.toDto(paymentService.getPaymentById(id)));
  }

  @GetMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<List<PaymentResponseDto>> getAllUsers() {
    List<Payment> Payments = paymentService.getAllPayments();
    return new ResponseEntity<>(mapper.toDtoList(Payments), HttpStatus.OK);
  }

  @PutMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<PaymentResponseDto> updateUser(@PathVariable Long id, @RequestBody PaymentRequestDto updatedPayment) {
    Payment updated = paymentService.updatePayment(id, updatedPayment);
    if (updated != null) {
      return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @PatchMapping("/{id}/{status}")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public ResponseEntity<PaymentResponseDto> updatePaymentStatus(@PathVariable("id") Long id, @PathVariable("status") String status) {
    Payment payment = paymentService.changePaymentStatus(id, PaymentStatus.valueOf(status));
    return new ResponseEntity<>(mapper.toDto(payment), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
    paymentService.deletePayment(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
