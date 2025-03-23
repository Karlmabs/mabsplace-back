package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionMapper;
import com.mabsplace.mabsplaceback.domain.services.PackageSubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller to handle package subscription operations
 */
@RestController
@RequestMapping("/api/package-subscriptions")
public class PackageSubscriptionController {
    private static final Logger logger = LoggerFactory.getLogger(PackageSubscriptionController.class);
    
    private final PackageSubscriptionService packageSubscriptionService;
    private final SubscriptionMapper subscriptionMapper;
    
    public PackageSubscriptionController(
            PackageSubscriptionService packageSubscriptionService,
            SubscriptionMapper subscriptionMapper) {
        this.packageSubscriptionService = packageSubscriptionService;
        this.subscriptionMapper = subscriptionMapper;
    }
    
    /**
     * Create a new package subscription
     */
    @PostMapping("/subscribe")
    public ResponseEntity<SubscriptionResponseDto> createPackageSubscription(
            @RequestParam Long userId,
            @RequestParam Long packagePlanId,
            @RequestParam(required = false) String promoCode) {
        
        logger.info("API request to create package subscription for user ID: {} with package plan ID: {}",
                userId, packagePlanId);
        
        Subscription subscription = packageSubscriptionService.createPackageSubscription(
                userId, packagePlanId, promoCode);
        
        return new ResponseEntity<>(subscriptionMapper.toDto(subscription), HttpStatus.CREATED);
    }
    
    /**
     * Cancel a package subscription
     */
    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<Map<String, String>> cancelPackageSubscription(
            @PathVariable Long subscriptionId) {
        
        logger.info("API request to cancel package subscription with ID: {}", subscriptionId);
        
        packageSubscriptionService.cancelPackageSubscription(subscriptionId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Package subscription cancelled successfully");
        
        return ResponseEntity.ok(response);
    }
}