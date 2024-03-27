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
        logger.info("Making payment with request: {}", paymentRequest);
        return coolPayService.makePayment(paymentRequest);
    }

    @PostMapping("/payin/authorize")
    public Object authorizePayment(@RequestBody AuthorizationRequest authorizationRequest) {
        logger.info("Authorizing payment with request: {}", authorizationRequest);
        return coolPayService.authorizePayment(authorizationRequest);
    }

    @PostMapping("/payout")
    public Object processPayout(@RequestBody PayoutRequest payoutRequest) {
        logger.info("Processing payout with request: {}", payoutRequest);
        return coolPayService.processPayout(payoutRequest);
    }

    @GetMapping("/checkTransactionStatus/{transactionRef}")
    public Object checkStatus(@PathVariable String transactionRef) {
        logger.info("Checking transaction status for ref: {}", transactionRef);
        return coolPayService.checkTransactionStatus(transactionRef);
    }

    @GetMapping("/checkBalance")
    public Object checkBalance() {
        logger.info("Checking balance");
        return coolPayService.checkBalance();
    }


}
