package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.coolpay.AuthorizationRequest;
import com.mabsplace.mabsplaceback.domain.dtos.coolpay.PaymentRequest;
import com.mabsplace.mabsplaceback.domain.dtos.coolpay.PayoutRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
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

        logger.info("Starting makePayment for transactionRef: {}", paymentRequest.getApp_transaction_ref());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // Wrapping the DTO and headers in an HttpEntity
        HttpEntity<PaymentRequest> entity = new HttpEntity<>(paymentRequest, headers);
        logger.debug("Payment request entity: {}", entity);

        try {
            Object response = restTemplate.postForObject(url, entity, Object.class);
            logger.info("Payment successful for transactionRef: {}", paymentRequest.getApp_transaction_ref());
            logger.debug("Payment response: {}", response);
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Error during makePayment for transactionRef: {}. Error: {}", paymentRequest.getApp_transaction_ref(), e.getMessage());
            throw e;
        } finally {
            logger.info("Completed makePayment for transactionRef: {}", paymentRequest.getApp_transaction_ref());
        }
    }

    public Object authorizePayment(AuthorizationRequest authorizationRequest) {
        String url = baseUrl + "/payin/authorize";

        logger.info("Authorizing payment for transactionRef: {}", authorizationRequest.getTransactionRef());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<AuthorizationRequest> entity = new HttpEntity<>(authorizationRequest, headers);
        logger.debug("Authorization request entity: {}", entity);

        try {
            Object response = restTemplate.postForObject(url, entity, Object.class);
            logger.info("Authorization successful for transactionRef: {}", authorizationRequest.getTransactionRef());
            logger.debug("Authorization response: {}", response);
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Error during authorizePayment for transactionRef: {}. Error: {}", authorizationRequest.getTransactionRef(), e.getMessage());
            throw e;
        } finally {
            logger.info("Completed authorizePayment for transactionRef: {}", authorizationRequest.getTransactionRef());
        }
    }

    public Object processPayout(PayoutRequest payoutRequest) {
        String url = baseUrl + "/payout";

        logger.info("Processing payout for transactionRef: {}", payoutRequest.getApp_transaction_ref());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-PRIVATE-KEY", privateKey);

        HttpEntity<PayoutRequest> entity = new HttpEntity<>(payoutRequest, headers);
        logger.debug("Payout request entity: {}", entity);

        try {
            Object response = restTemplate.postForObject(url, entity, Object.class);
            logger.info("Payout processed successfully for transactionRef: {}", payoutRequest.getApp_transaction_ref());
            logger.debug("Payout response: {}", response);
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Error during processPayout for transactionRef: {}. Error: {}", payoutRequest.getApp_transaction_ref(), e.getMessage());
            throw e;
        } finally {
            logger.info("Completed processPayout for transactionRef: {}", payoutRequest.getApp_transaction_ref());
        }
    }

    public Object checkTransactionStatus(String transactionRef) {
        String url = baseUrl + "/checkStatus/" + transactionRef;

        logger.info("Checking transaction status for transactionRef: {}", transactionRef);

        try {
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            logger.info("Transaction status retrieved successfully for transactionRef: {}", transactionRef);
            logger.debug("Transaction status response: {}", response.getBody());
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Error during checkTransactionStatus for transactionRef: {}. Error: {}", transactionRef, e.getMessage());
            throw e;
        } finally {
            logger.info("Completed checkTransactionStatus for transactionRef: {}", transactionRef);
        }
    }

    public Object checkBalance() {
        String url = baseUrl + "/balance";

        logger.info("Checking account balance");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-PRIVATE-KEY", privateKey);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        logger.debug("Balance check request entity: {}", entity);

        try {
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);
            logger.info("Balance retrieved successfully");
            logger.debug("Balance response: {}", response.getBody());
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Error during checkBalance. Error: {}", e.getMessage());
            throw e;
        } finally {
            logger.info("Completed checkBalance operation");
        }
    }

    public Object generatePaymentLink(PaymentRequest paymentRequest) {
        String url = baseUrl + "/paylink?ref=" + paymentRequest.getApp_transaction_ref();

        logger.info("Generating payment link for transactionRef: {}", paymentRequest.getApp_transaction_ref());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<PaymentRequest> entity = new HttpEntity<>(paymentRequest, headers);
        logger.debug("Payment link request entity: {}", entity);

        try {
            Object response = restTemplate.postForObject(url, entity, Object.class);
            logger.info("Payment link generated successfully for transactionRef: {}", paymentRequest.getApp_transaction_ref());
            logger.debug("Payment link response: {}", response);
            return response;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("Error during generatePaymentLink for transactionRef: {}. Error: {}", paymentRequest.getApp_transaction_ref(), e.getMessage());
            throw e;
        } finally {
            logger.info("Completed generatePaymentLink for transactionRef: {}", paymentRequest.getApp_transaction_ref());
        }
    }
}
