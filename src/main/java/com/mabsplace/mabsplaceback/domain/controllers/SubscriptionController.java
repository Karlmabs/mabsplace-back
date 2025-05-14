package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Profile;
import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.enums.ProfileStatus;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionMapper;
import com.mabsplace.mabsplaceback.domain.repositories.ProfileRepository;
import com.mabsplace.mabsplaceback.domain.repositories.SubscriptionRepository;
import com.mabsplace.mabsplaceback.domain.services.SubscriptionService;
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
  private final SubscriptionRepository subscriptionRepository;
  private final ProfileRepository profileRepository;

  public SubscriptionController(SubscriptionService subscriptionService, SubscriptionMapper mapper,
                                SubscriptionRepository subscriptionRepository,
                                ProfileRepository profileRepository) {
    this.subscriptionService = subscriptionService;
    this.mapper = mapper;
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

  @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'GET_SUBSCRIPTIONS')")
  @GetMapping
  public ResponseEntity<List<SubscriptionResponseDto>> getAllSubscriptions() {
    logger.info("Fetching all subscriptions");
    List<Subscription> subscriptions = subscriptionService.getAllSubscriptions();
    logger.info("Fetched {} subscriptions", subscriptions.size());
    return new ResponseEntity<>(mapper.toDtoList(subscriptions), HttpStatus.OK);
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

}
