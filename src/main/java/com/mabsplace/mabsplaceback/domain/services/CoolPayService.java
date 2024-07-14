package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.coolpay.AuthorizationRequest;
import com.mabsplace.mabsplaceback.domain.dtos.coolpay.PaymentRequest;
import com.mabsplace.mabsplaceback.domain.dtos.coolpay.PayoutRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class CoolPayService {

    private final RestTemplate restTemplate;

    @Value("${mabsplace.app.baseUrl}")
    private String baseUrl;

    @Value("${mabsplace.app.privateKey}")
    private String privateKey;

    private static final Logger logger = LoggerFactory.getLogger(CoolPayService.class);

    public CoolPayService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Object makePayment(PaymentRequest paymentRequest) {
        String url = baseUrl + "/payin";

        logger.info("Making payment request to CoolPay API");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // Wrapping the DTO and headers in an HttpEntity
        HttpEntity<PaymentRequest> entity = new HttpEntity<>(paymentRequest, headers);

        logger.info("Payment request: {}", entity);

        // Making the POST request
        return restTemplate.postForObject(url, entity, Object.class);
    }

    public Object authorizePayment(AuthorizationRequest authorizationRequest) {
        String url = baseUrl + "/payin/authorize";

        logger.info("Authorizing payment request to CoolPay API");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<AuthorizationRequest> entity = new HttpEntity<>(authorizationRequest, headers);

        logger.info("Authorization request: {}", entity);

        return restTemplate.postForObject(url, entity, Object.class);
    }

    public Object processPayout(PayoutRequest payoutRequest) {
        String url = baseUrl + "/payout";

        logger.info("Processing payout request to CoolPay API");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-PRIVATE-KEY", privateKey);

        HttpEntity<PayoutRequest> entity = new HttpEntity<>(payoutRequest, headers);

        logger.info("Payout request: {}", entity);

        return restTemplate.postForObject(url, entity, Object.class);
    }

    public Object checkTransactionStatus(String transactionRef) {
        String url = baseUrl + "/checkStatus/" + transactionRef;

        logger.info("Checking transaction status for transaction ref {}", transactionRef);

        ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);

        logger.info("Transaction status: {}", response.getBody());

        return response.getBody();
    }

    public Object checkBalance() {
        String url = baseUrl + "/balance";

        logger.info("Checking balance");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-PRIVATE-KEY", privateKey);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);

        logger.info("Balance: {}", response.getBody());

        return response.getBody();
    }

    public Object generatePaymentLink(PaymentRequest paymentRequest) {
        String url = baseUrl + "/paylink?ref="+paymentRequest.getApp_transaction_ref();

        logger.info("Generating payment link request to CoolPay API");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<PaymentRequest> entity = new HttpEntity<>(paymentRequest, headers);

        logger.info("Payment link request: {}", entity);

        return restTemplate.postForObject(url, entity, Object.class);
    }




}
