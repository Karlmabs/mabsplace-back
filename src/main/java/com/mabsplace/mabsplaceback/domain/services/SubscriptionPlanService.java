package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import com.mabsplace.mabsplaceback.domain.repositories.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionPlanService {

  private final SubscriptionPlanRepository subscriptionPlanRepository;

  public SubscriptionPlanService(SubscriptionPlanRepository subscriptionPlanRepository) {
    this.subscriptionPlanRepository = subscriptionPlanRepository;
  }

  public SubscriptionPlan createSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
    return subscriptionPlanRepository.save(subscriptionPlan);
  }

  public List<SubscriptionPlan> getAllSubscriptionPlans() {
    return subscriptionPlanRepository.findAll();
  }

  public SubscriptionPlan updateSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
    return subscriptionPlanRepository.save(subscriptionPlan);
  }

  public void deleteSubscriptionPlanById(Long id) {
    subscriptionPlanRepository.deleteById(id);
  }

  public SubscriptionPlan getSubscriptionPlanById(Long id) {
    return subscriptionPlanRepository.findById(id).orElse(null);
  }
}
