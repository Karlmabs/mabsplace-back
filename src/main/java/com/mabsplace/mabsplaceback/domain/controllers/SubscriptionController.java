package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscription.SubscriptionResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionMapper;
import com.mabsplace.mabsplaceback.domain.services.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
  
  private final SubscriptionService subscriptionService;
  private final SubscriptionMapper mapper;
  public SubscriptionController(SubscriptionService subscriptionService, SubscriptionMapper mapper) {
    this.subscriptionService = subscriptionService;
    this.mapper = mapper;
  }

  @PostMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<SubscriptionResponseDto> createSubscription(@RequestBody SubscriptionRequestDto subscriptionRequestDto) {
    Subscription createdSubscription = subscriptionService.createSubscription(subscriptionRequestDto);
    return new ResponseEntity<>(mapper.toDto(createdSubscription), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<SubscriptionResponseDto> getSubscriptionById(@PathVariable Long id) {
    return ResponseEntity.ok(mapper.toDto(subscriptionService.getSubscriptionById(id)));
  }

  @GetMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<List<SubscriptionResponseDto>> getAllSubscriptions() {
    List<Subscription> Subscriptions = subscriptionService.getAllSubscriptions();
    return new ResponseEntity<>(mapper.toDtoList(Subscriptions), HttpStatus.OK);
  }

  @PutMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<SubscriptionResponseDto> updateSubscription(@PathVariable Long id, @RequestBody SubscriptionRequestDto updatedSubscription) {
    Subscription updated = subscriptionService.updateSubscription(id, updatedSubscription);
    if (updated != null) {
      return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @DeleteMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<Void> deleteSubscription(@PathVariable Long id) {
    subscriptionService.deleteSubscription(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
