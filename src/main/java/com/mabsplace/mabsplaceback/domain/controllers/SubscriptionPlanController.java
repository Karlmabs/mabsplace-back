package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionPlanMapper;
import com.mabsplace.mabsplaceback.domain.services.SubscriptionPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptionPlans")
public class SubscriptionPlanController {
  
  private final SubscriptionPlanService subscriptionPlanService;
  private final SubscriptionPlanMapper mapper;

  public SubscriptionPlanController(SubscriptionPlanService subscriptionPlanService, SubscriptionPlanMapper mapper) {
    this.subscriptionPlanService = subscriptionPlanService;
    this.mapper = mapper;
  }

  @PostMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<SubscriptionPlanResponseDto> createSubscriptionPlan(@RequestBody SubscriptionPlanRequestDto subscriptionPlanRequestDto) {
    SubscriptionPlan createdSubscriptionPlan = subscriptionPlanService.createSubscriptionPlan(subscriptionPlanRequestDto);
    return new ResponseEntity<>(mapper.toDto(createdSubscriptionPlan), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<SubscriptionPlanResponseDto> getSubscriptionPlanById(@PathVariable Long id) {
    return ResponseEntity.ok(mapper.toDto(subscriptionPlanService.getSubscriptionPlanById(id)));
  }

  @GetMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<List<SubscriptionPlanResponseDto>> getAllSubscriptionPlans() {
    List<SubscriptionPlan> SubscriptionPlans = subscriptionPlanService.getAllSubscriptionPlans();
    return new ResponseEntity<>(mapper.toDtoList(SubscriptionPlans), HttpStatus.OK);
  }

  @PutMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<SubscriptionPlanResponseDto> updateSubscriptionPlan(@PathVariable Long id, @RequestBody SubscriptionPlanRequestDto updatedSubscriptionPlan) {
    SubscriptionPlan updated = subscriptionPlanService.updateSubscriptionPlan(id, updatedSubscriptionPlan);
    if (updated != null) {
      return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @DeleteMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<Void> deleteSubscriptionPlan(@PathVariable Long id) {
    subscriptionPlanService.deleteSubscriptionPlan(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
