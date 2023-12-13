package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;

  public SubscriptionService(SubscriptionRepository subscriptionRepository) {
    this.subscriptionRepository = subscriptionRepository;
  }

  public Subscription createSubscription(Subscription subscription) {
    return subscriptionRepository.save(subscription);
  }

  public Subscription getSubscription(Long id) {
    return subscriptionRepository.findById(id).orElse(null);
  }

  public Subscription updateSubscription(Subscription subscription) {
    return subscriptionRepository.save(subscription);
  }

  public void deleteSubscription(Long id) {
    subscriptionRepository.deleteById(id);
  }
}
