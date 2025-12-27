package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionRenewalRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionLightweightResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Profile;
import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.enums.ProfileStatus;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionMapper;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionLightweightMapper;
import com.mabsplace.mabsplaceback.domain.repositories.ProfileRepository;
import com.mabsplace.mabsplaceback.domain.repositories.SubscriptionRepository;
import com.mabsplace.mabsplaceback.domain.services.SubscriptionService;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

  private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

  private final SubscriptionService subscriptionService;
  private final SubscriptionMapper mapper;
  private final SubscriptionLightweightMapper lightweightMapper;
  private final SubscriptionRepository subscriptionRepository;
  private final ProfileRepository profileRepository;

  public SubscriptionController(SubscriptionService subscriptionService, SubscriptionMapper mapper,
                                SubscriptionLightweightMapper lightweightMapper,
                                SubscriptionRepository subscriptionRepository,
                                ProfileRepository profileRepository) {
    this.subscriptionService = subscriptionService;
    this.mapper = mapper;
    this.lightweightMapper = lightweightMapper;
    this.subscriptionRepository = subscriptionRepository;
    this.profileRepository = profileRepository;
  }

  @PostMapping
  public ResponseEntity<SubscriptionResponseDto> createSubscription(@RequestBody SubscriptionRequestDto subscriptionRequestDto) {
    logger.info("Creating subscription with request: {}", subscriptionRequestDto);
    Subscription createdSubscription = subscriptionService.createSubscription(subscriptionRequestDto);
    logger.info("Created subscription: {}", createdSubscription);
    return new ResponseEntity<>(mapper.toDto(createdSubscription), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  public ResponseEntity<SubscriptionResponseDto> getSubscriptionById(@PathVariable Long id) {
    logger.info("Fetching subscription with ID: {}", id);
    Subscription subscription = subscriptionService.getSubscriptionById(id);
    logger.info("Fetched subscription: {}", subscription);
    return ResponseEntity.ok(mapper.toDto(subscription));
  }

  @GetMapping("/{id}/lightweight")
  public ResponseEntity<SubscriptionLightweightResponseDto> getSubscriptionByIdLightweight(@PathVariable Long id) {
    logger.info("Fetching lightweight subscription with ID: {}", id);
    Subscription subscription = subscriptionService.getSubscriptionById(id);
    logger.info("Fetched lightweight subscription: {}", subscription);
    return ResponseEntity.ok(lightweightMapper.toDto(subscription));
  }

  @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'GET_SUBSCRIPTIONS')")
  @GetMapping
  public ResponseEntity<List<SubscriptionResponseDto>> getAllSubscriptions() {
    logger.info("Fetching all subscriptions");
    List<Subscription> subscriptions = subscriptionService.getAllSubscriptions();
    logger.info("Fetched {} subscriptions", subscriptions.size());
    return new ResponseEntity<>(mapper.toDtoList(subscriptions), HttpStatus.OK);
  }

  @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'GET_SUBSCRIPTIONS')")
  @GetMapping("/lightweight")
  public ResponseEntity<List<SubscriptionLightweightResponseDto>> getAllSubscriptionsLightweight() {
    logger.info("Fetching all subscriptions lightweight");
    List<Subscription> subscriptions = subscriptionService.getAllSubscriptions();
    logger.info("Fetched {} subscriptions lightweight", subscriptions.size());
    return new ResponseEntity<>(lightweightMapper.toDtoList(subscriptions), HttpStatus.OK);
  }

  @PutMapping("/{id}")
  public ResponseEntity<SubscriptionResponseDto> updateSubscription(@PathVariable Long id, @RequestBody SubscriptionRequestDto updatedSubscription) {
    logger.info("Updating subscription with ID: {}, Request: {}", id, updatedSubscription);
    Subscription updated = subscriptionService.updateSubscription(id, updatedSubscription);
    if (updated != null) {
      logger.info("Updated subscription successfully: {}", updated);
      return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
    }
    logger.warn("Subscription not found with ID: {}", id);
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSubscription(@PathVariable Long id) {
    logger.info("Deleting subscription with ID: {}", id);
    subscriptionService.deleteSubscription(id);
    logger.info("Deleted subscription successfully and set profile status to INACTIVE, ID: {}", id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<SubscriptionResponseDto>> getSubscriptionsByUserId(@PathVariable Long userId) {
    logger.info("Fetching subscriptions for user ID: {}", userId);
    List<Subscription> subscriptions = subscriptionService.getSubscriptionsByUserId(userId);
    logger.info("Fetched {} subscriptions for user ID: {}", subscriptions.size(), userId);
    return ResponseEntity.ok(mapper.toDtoList(subscriptions));
  }

  @GetMapping("/user/{userId}/lightweight")
  public ResponseEntity<List<SubscriptionLightweightResponseDto>> getSubscriptionsByUserIdLightweight(@PathVariable Long userId) {
    logger.info("Fetching lightweight subscriptions for user ID: {}", userId);
    List<Subscription> subscriptions = subscriptionService.getSubscriptionsByUserId(userId);
    logger.info("Fetched {} lightweight subscriptions for user ID: {}", subscriptions.size(), userId);
    return ResponseEntity.ok(lightweightMapper.toDtoList(subscriptions));
  }

  @PostMapping("/{id}/renew")
  public ResponseEntity<?> renewSubscription(
          @PathVariable Long id,
          @RequestParam(required = false) Long planId) {
    logger.info("Manual renewal requested for subscription ID: {}, with plan ID: {}", id, planId);
    try {
      Subscription renewedSubscription = subscriptionService.renewSubscriptionManually(id, planId);
      logger.info("Successfully renewed subscription ID: {}", id);
      return new ResponseEntity<>(mapper.toDto(renewedSubscription), HttpStatus.OK);
    } catch (IllegalStateException e) {
      logger.error("Renewal failed for subscription ID: {} - {}", id, e.getMessage());
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (ResourceNotFoundException e) {
      logger.error("Subscription not found for renewal - ID: {}", id);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      logger.error("Unexpected error renewing subscription ID: {}", id, e);
      return new ResponseEntity<>("An unexpected error occurred while renewing the subscription", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/{id}/renew-with-payment")
  public ResponseEntity<?> renewActiveSubscriptionWithPayment(
          @PathVariable Long id,
          @RequestBody SubscriptionRenewalRequestDto renewalRequest) {
    logger.info("Paid renewal requested for active subscription ID: {}", id);
    try {
      Subscription renewed = subscriptionService.renewActiveSubscriptionWithPayment(
              id,
              renewalRequest.getNewPlanId(),
              renewalRequest.getPromoCode()
      );
      logger.info("Successfully renewed active subscription ID: {}", id);
      return ResponseEntity.ok(mapper.toDto(renewed));
    } catch (IllegalStateException e) {
      logger.error("Renewal failed - invalid state for subscription ID: {} - {}", id, e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (ResourceNotFoundException e) {
      logger.error("Resource not found for renewal - subscription ID: {} - {}", id, e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Runtime error during paid renewal for subscription ID: {} - {}", id, e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      logger.error("Unexpected error during paid renewal for subscription ID: {}", id, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("Renewal failed: " + e.getMessage());
    }
  }

}
