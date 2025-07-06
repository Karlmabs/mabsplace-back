package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionPlanMapper;
import com.mabsplace.mabsplaceback.domain.services.SubscriptionPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptionPlans")
public class SubscriptionPlanController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionPlanController.class);

    private final SubscriptionPlanService subscriptionPlanService;
    private final SubscriptionPlanMapper mapper;

    public SubscriptionPlanController(SubscriptionPlanService subscriptionPlanService, @Qualifier("customSubscriptionPlanMapper") SubscriptionPlanMapper mapper) {
        this.subscriptionPlanService = subscriptionPlanService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<SubscriptionPlanResponseDto> createSubscriptionPlan(@RequestBody SubscriptionPlanRequestDto subscriptionPlanRequestDto) {
        logger.info("Creating subscription plan with request: {}", subscriptionPlanRequestDto);
        SubscriptionPlan createdSubscriptionPlan = subscriptionPlanService.createSubscriptionPlan(subscriptionPlanRequestDto);
        logger.info("Created subscription plan: {}", createdSubscriptionPlan);
        return new ResponseEntity<>(mapper.toDto(createdSubscriptionPlan), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionPlanResponseDto> getSubscriptionPlanById(@PathVariable Long id) {
        logger.info("Fetching subscription plan with ID: {}", id);
        SubscriptionPlan subscriptionPlan = subscriptionPlanService.getSubscriptionPlanById(id);
        logger.info("Fetched subscription plan: {}", subscriptionPlan);
        return ResponseEntity.ok(mapper.toDto(subscriptionPlan));
    }

    @GetMapping("/myService/{myServiceId}")
    public ResponseEntity<List<SubscriptionPlanResponseDto>> getSubscriptionPlansByMyServiceId(@PathVariable Long myServiceId) {
        logger.info("Fetching subscription plans by myService ID: {}", myServiceId);
        List<SubscriptionPlan> subscriptionPlans = subscriptionPlanService.getSubscriptionPlansByMyServiceId(myServiceId);
        logger.info("Fetched {} subscription plans for myService ID: {}", subscriptionPlans.size(), myServiceId);
        return new ResponseEntity<>(mapper.toDtoList(subscriptionPlans), HttpStatus.OK);
    }

    @GetMapping("/myService/{myServiceId}/non-trial")
    public ResponseEntity<List<SubscriptionPlanResponseDto>> getNonTrialSubscriptionPlansByMyServiceId(@PathVariable Long myServiceId) {
        logger.info("Fetching non-trial subscription plans by myService ID: {}", myServiceId);
        List<SubscriptionPlan> subscriptionPlans = subscriptionPlanService.getNonTrialSubscriptionPlansByMyServiceId(myServiceId);
        logger.info("Fetched {} non-trial subscription plans for myService ID: {}", subscriptionPlans.size(), myServiceId);
        return new ResponseEntity<>(mapper.toDtoList(subscriptionPlans), HttpStatus.OK);
    }

    @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'GET_SUBSCRIPTION_PLANS')")
    @GetMapping
    public ResponseEntity<List<SubscriptionPlanResponseDto>> getAllSubscriptionPlans() {
        logger.info("Fetching all subscription plans");
        List<SubscriptionPlan> subscriptionPlans = subscriptionPlanService.getAllSubscriptionPlans();
        logger.info("Fetched {} subscription plans", subscriptionPlans.size());
        return new ResponseEntity<>(mapper.toDtoList(subscriptionPlans), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionPlanResponseDto> updateSubscriptionPlan(@PathVariable Long id, @RequestBody SubscriptionPlanRequestDto updatedSubscriptionPlan) {
        logger.info("Updating subscription plan with ID: {}, Request: {}", id, updatedSubscriptionPlan);
        SubscriptionPlan updated = subscriptionPlanService.updateSubscriptionPlan(id, updatedSubscriptionPlan);
        if (updated != null) {
            logger.info("Updated subscription plan successfully: {}", updated);
            return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
        }
        logger.warn("Subscription plan not found with ID: {}", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscriptionPlan(@PathVariable Long id) {
        logger.info("Deleting subscription plan with ID: {}", id);
        subscriptionPlanService.deleteSubscriptionPlan(id);
        logger.info("Deleted subscription plan successfully with ID: {}", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
