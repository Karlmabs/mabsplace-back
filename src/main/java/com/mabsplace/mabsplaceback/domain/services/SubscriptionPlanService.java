package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import com.mabsplace.mabsplaceback.domain.mappers.SubscriptionPlanMapper;
import com.mabsplace.mabsplaceback.domain.repositories.MyServiceRepository;
import com.mabsplace.mabsplaceback.domain.repositories.SubscriptionPlanRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionPlanService {

  private final SubscriptionPlanRepository subscriptionPlanRepository;
  private final SubscriptionPlanMapper mapper;
  private final MyServiceRepository myServiceRepository;

  public SubscriptionPlanService(SubscriptionPlanRepository subscriptionPlanRepository, SubscriptionPlanMapper mapper, MyServiceRepository myServiceRepository) {
    this.subscriptionPlanRepository = subscriptionPlanRepository;
    this.mapper = mapper;
    this.myServiceRepository = myServiceRepository;
  }

  public SubscriptionPlan createSubscriptionPlan(SubscriptionPlanRequestDto subscriptionPlan) throws ResourceNotFoundException{
    SubscriptionPlan newSubscriptionPlan = mapper.toEntity(subscriptionPlan);
    newSubscriptionPlan.setMyService(myServiceRepository.findById(subscriptionPlan.getMyServiceId()).orElseThrow(() -> new ResourceNotFoundException("MyService", "id", subscriptionPlan.getMyServiceId())));
    return subscriptionPlanRepository.save(newSubscriptionPlan);
  }

  public List<SubscriptionPlan> getAllSubscriptionPlans() {
    return subscriptionPlanRepository.findAll();
  }

  public SubscriptionPlan getSubscriptionPlanById(Long id) {
    return subscriptionPlanRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", id));
  }

  public void deleteSubscriptionPlan(Long id) {
    subscriptionPlanRepository.deleteById(id);
  }

  public SubscriptionPlan updateSubscriptionPlan(Long id, SubscriptionPlanRequestDto updatedSubscriptionPlan) throws ResourceNotFoundException {
    SubscriptionPlan target = subscriptionPlanRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("SubscriptionPlan", "id", id));
    SubscriptionPlan updated = mapper.partialUpdate(updatedSubscriptionPlan, target);
    updated.setMyService(myServiceRepository.findById(updatedSubscriptionPlan.getMyServiceId()).orElseThrow(() -> new ResourceNotFoundException("MyService", "id", updatedSubscriptionPlan.getMyServiceId())));
    return subscriptionPlanRepository.save(updated);
  }
}
