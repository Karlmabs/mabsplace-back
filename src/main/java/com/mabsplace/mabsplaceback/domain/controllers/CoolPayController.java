package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.coolpay.AuthorizationRequest;
import com.mabsplace.mabsplaceback.domain.dtos.coolpay.PaymentRequest;
import com.mabsplace.mabsplaceback.domain.dtos.coolpay.PayoutRequest;
import com.mabsplace.mabsplaceback.domain.services.CoolPayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coolpay")
public class CoolPayController {

    private final CoolPayService coolPayService;
    private static final Logger logger = LoggerFactory.getLogger(CoolPayController.class);

    public CoolPayController(CoolPayService coolPayService) {
        this.coolPayService = coolPayService;
    }

    @PostMapping("/payin")
    public Object makePayment(@RequestBody PaymentRequest paymentRequest) {
        logger.info("Received payment request: {}", paymentRequest);
        Object response = coolPayService.makePayment(paymentRequest);
        logger.info("Payment response: {}", response);
        return response;
    }

    @PostMapping("/payin/authorize")
    public Object authorizePayment(@RequestBody AuthorizationRequest authorizationRequest) {
        logger.info("Received authorization request: {}", authorizationRequest);
        Object response = coolPayService.authorizePayment(authorizationRequest);
        logger.info("Authorization response: {}", response);
        return response;
    }

    @PostMapping("/payout")
    public Object processPayout(@RequestBody PayoutRequest payoutRequest) {
        logger.info("Received payout request: {}", payoutRequest);
        Object response = coolPayService.processPayout(payoutRequest);
        logger.info("Payout response: {}", response);
        return response;
    }

    @GetMapping("/checkTransactionStatus/{transactionRef}")
    public Object checkStatus(@PathVariable String transactionRef) {
        logger.info("Checking transaction status for ref: {}", transactionRef);
        return coolPayService.checkTransactionStatus(transactionRef);
    }

    @GetMapping("/checkBalance")
    public Object checkBalance() {
        logger.info("Balance check requested");
        Object balance = coolPayService.checkBalance();
        logger.info("Current balance: {}", balance);
        return balance;
    }

}
