package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.contributor.*;
import com.mabsplace.mabsplaceback.domain.entities.ContributorPayment;
import com.mabsplace.mabsplaceback.domain.entities.ContributorPaymentConfig;
import com.mabsplace.mabsplaceback.domain.entities.GlobalPaymentSettings;
import com.mabsplace.mabsplaceback.domain.services.ContributorPaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contributor-payments")
@PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'MANAGE_CONTRIBUTOR_PAYMENTS')")
public class ContributorPaymentController {

    private static final Logger logger = LoggerFactory.getLogger(ContributorPaymentController.class);

    @Autowired
    private ContributorPaymentService contributorPaymentService;

    // ==================== Config Endpoints ====================

    @GetMapping("/configs")
    public ResponseEntity<List<ContributorPaymentConfigResponse>> getAllConfigs() {
        logger.info("Getting all contributor payment configs");
        List<ContributorPaymentConfig> configs = contributorPaymentService.getAllConfigs();
        List<ContributorPaymentConfigResponse> response = contributorPaymentService.toConfigResponseList(configs);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/configs/{id}")
    public ResponseEntity<ContributorPaymentConfigResponse> getConfigById(@PathVariable Long id) {
        logger.info("Getting contributor payment config with ID: {}", id);
        ContributorPaymentConfig config = contributorPaymentService.getConfigById(id);
        ContributorPaymentConfigResponse response = contributorPaymentService.toConfigResponse(config);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/configs")
    public ResponseEntity<ContributorPaymentConfigResponse> createConfig(@Valid @RequestBody CreateContributorConfigRequest request) {
        logger.info("Creating new contributor payment config for user ID: {}", request.getUserId());
        ContributorPaymentConfig config = contributorPaymentService.createConfig(request);
        ContributorPaymentConfigResponse response = contributorPaymentService.toConfigResponse(config);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/configs/{id}")
    public ResponseEntity<ContributorPaymentConfigResponse> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContributorConfigRequest request) {
        logger.info("Updating contributor payment config with ID: {}", id);
        ContributorPaymentConfig config = contributorPaymentService.updateConfig(id, request);
        ContributorPaymentConfigResponse response = contributorPaymentService.toConfigResponse(config);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/configs/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        logger.info("Deleting contributor payment config with ID: {}", id);
        contributorPaymentService.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Global Settings Endpoints ====================

    @GetMapping("/settings")
    public ResponseEntity<GlobalPaymentSettings> getGlobalSettings() {
        logger.info("Getting global payment settings");
        GlobalPaymentSettings settings = contributorPaymentService.getGlobalSettings();
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/settings")
    public ResponseEntity<GlobalPaymentSettings> updateGlobalSettings(@Valid @RequestBody UpdateGlobalSettingsRequest request) {
        logger.info("Updating global payment settings");
        GlobalPaymentSettings settings = contributorPaymentService.updateGlobalSettings(request);
        return ResponseEntity.ok(settings);
    }

    // ==================== Payment Processing Endpoints ====================

    @PostMapping("/process/{configId}")
    public ResponseEntity<ContributorPaymentResponse> processPayment(@PathVariable Long configId) {
        logger.info("Processing manual payment for config ID: {}", configId);
        ContributorPayment payment = contributorPaymentService.processPayment(configId);
        ContributorPaymentResponse response = contributorPaymentService.toPaymentResponse(payment);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/retry/{paymentId}")
    public ResponseEntity<ContributorPaymentResponse> retryFailedPayment(@PathVariable Long paymentId) {
        logger.info("Retrying failed payment with ID: {}", paymentId);
        ContributorPayment payment = contributorPaymentService.retryFailedPayment(paymentId);
        ContributorPaymentResponse response = contributorPaymentService.toPaymentResponse(payment);
        return ResponseEntity.ok(response);
    }

    // ==================== Payment History Endpoints ====================

    @GetMapping("/history")
    public ResponseEntity<List<ContributorPaymentResponse>> getPaymentHistory() {
        logger.info("Getting all payment history");
        List<ContributorPayment> history = contributorPaymentService.getPaymentHistory();
        List<ContributorPaymentResponse> response = contributorPaymentService.toPaymentResponseList(history);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/user/{userId}")
    public ResponseEntity<List<ContributorPaymentResponse>> getPaymentHistoryByUser(@PathVariable Long userId) {
        logger.info("Getting payment history for user ID: {}", userId);
        List<ContributorPayment> history = contributorPaymentService.getPaymentHistoryByUser(userId);
        List<ContributorPaymentResponse> response = contributorPaymentService.toPaymentResponseList(history);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/period/{period}")
    public ResponseEntity<List<ContributorPaymentResponse>> getPaymentHistoryByPeriod(@PathVariable String period) {
        logger.info("Getting payment history for period: {}", period);
        List<ContributorPayment> history = contributorPaymentService.getPaymentHistoryByPeriod(period);
        List<ContributorPaymentResponse> response = contributorPaymentService.toPaymentResponseList(history);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/failed")
    public ResponseEntity<List<ContributorPaymentResponse>> getFailedPayments() {
        logger.info("Getting failed payments");
        List<ContributorPayment> failedPayments = contributorPaymentService.getFailedPayments();
        List<ContributorPaymentResponse> response = contributorPaymentService.toPaymentResponseList(failedPayments);
        return ResponseEntity.ok(response);
    }

    // ==================== Preview Endpoint ====================

    @GetMapping("/preview")
    public ResponseEntity<PaymentPreviewResponse> previewNextPayments() {
        logger.info("Generating payment preview");
        PaymentPreviewResponse preview = contributorPaymentService.previewNextPayments();
        return ResponseEntity.ok(preview);
    }

    // ==================== Exception Handler ====================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        logger.error("Error in contributor payment controller: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    // Error response DTO
    private record ErrorResponse(String message) {}
}
