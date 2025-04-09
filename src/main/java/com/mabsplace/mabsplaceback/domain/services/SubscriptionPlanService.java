package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.subscriptionPlan.SubscriptionPlanRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import com.mabsplace.mabsplaceback.domain.mappers.CustomSubscriptionPlanMapper;
import com.mabsplace.mabsplaceback.domain.repositories.CurrencyRepository;
import com.mabsplace.mabsplaceback.domain.repositories.MyServiceRepository;
import com.mabsplace.mabsplaceback.domain.repositories.SubscriptionPlanRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionPlanService {

  private static final Logger logger = LoggerFactory.getLogger(SubscriptionPlanService.class);

  private final SubscriptionPlanRepository subscriptionPlanRepository;
  private final CustomSubscriptionPlanMapper mapper;
  private final MyServiceRepository myServiceRepository;
  private final CurrencyRepository currencyRepository;
  private final SubscriptionDiscountService discountService;

  public SubscriptionPlanService(SubscriptionPlanRepository subscriptionPlanRepository, CustomSubscriptionPlanMapper mapper, MyServiceRepository myServiceRepository, CurrencyRepository currencyRepository, SubscriptionDiscountService discountService) {
    this.subscriptionPlanRepository = subscriptionPlanRepository;
    this.mapper = mapper;
    this.myServiceRepository = myServiceRepository;
    this.currencyRepository = currencyRepository;
    this.discountService = discountService;
  }

  private void validateSubscriptionPlan(SubscriptionPlanRequestDto subscriptionPlan, Long excludeId) {
    // Check for duplicate period
    boolean periodExists = excludeId == null ?
        subscriptionPlanRepository.existsByMyServiceIdAndPeriod(subscriptionPlan.getMyServiceId(), subscriptionPlan.getPeriod()) :
        subscriptionPlanRepository.existsByMyServiceIdAndPeriodAndIdNot(subscriptionPlan.getMyServiceId(), subscriptionPlan.getPeriod(), excludeId);

    if (periodExists) {
      logger.error("Subscription plan with period {} already exists for service ID: {}",
          subscriptionPlan.getPeriod(), subscriptionPlan.getMyServiceId());
      throw new IllegalStateException(
          String.format("A subscription plan with period %s already exists for this service",
              subscriptionPlan.getPeriod()));
    }

    // Check for duplicate name
    boolean nameExists = excludeId == null ?
        subscriptionPlanRepository.existsByMyServiceIdAndNameIgnoreCase(subscriptionPlan.getMyServiceId(), subscriptionPlan.getName()) :
        subscriptionPlanRepository.existsByMyServiceIdAndNameIgnoreCaseAndIdNot(subscriptionPlan.getMyServiceId(), subscriptionPlan.getName(), excludeId);

    if (nameExists) {
      logger.error("Subscription plan with name '{}' already exists for service ID: {}",
          subscriptionPlan.getName(), subscriptionPlan.getMyServiceId());
      throw new IllegalStateException(
          String.format("A subscription plan with name '%s' already exists for this service",
              subscriptionPlan.getName()));
    }
  }

  public SubscriptionPlan createSubscriptionPlan(SubscriptionPlanRequestDto subscriptionPlan) throws ResourceNotFoundException {
    logger.info("Creating subscription plan with data: {}", subscriptionPlan);

    // Validate unique constraints
    validateSubscriptionPlan(subscriptionPlan, null);

    SubscriptionPlan newSubscriptionPlan = mapper.toEntity(subscriptionPlan);
    newSubscriptionPlan.setMyService(myServiceRepository.findById(subscriptionPlan.getMyServiceId())
        .orElseThrow(() -> {
            logger.error("MyService not found with ID: {}", subscriptionPlan.getMyServiceId());
            return new ResourceNotFoundException("MyService", "id", subscriptionPlan.getMyServiceId());
        }));
    newSubscriptionPlan.setCurrency(currencyRepository.findById(subscriptionPlan.getCurrencyId())
        .orElseThrow(() -> {
            logger.error("Currency not found with ID: {}", subscriptionPlan.getCurrencyId());
            return new ResourceNotFoundException("Currency", "id", subscriptionPlan.getCurrencyId());
        }));

    SubscriptionPlan savedSubscriptionPlan = subscriptionPlanRepository.save(newSubscriptionPlan);
    logger.info("Subscription plan created successfully: {}", savedSubscriptionPlan);
    return savedSubscriptionPlan;
  }

  public List<SubscriptionPlan> getAllSubscriptionPlans() {
    logger.info("Fetching all subscription plans");
    List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll();
    logger.info("Fetched {} subscription plans", plans.size());
    return plans;
  }

  public SubscriptionPlan getSubscriptionPlanById(Long id) {
    logger.info("Fetching subscription plan with ID: {}", id);
    SubscriptionPlan plan = subscriptionPlanRepository.findById(id).orElseThrow(() -> {
        logger.error("SubscriptionPlan not found with ID: {}", id);
        return new ResourceNotFoundException("SubscriptionPlan", "id", id);
    });
    logger.info("Fetched subscription plan successfully: {}", plan);
    return plan;
  }

  public List<SubscriptionPlan> getSubscriptionPlansByMyServiceId(Long myServiceId) {
    logger.info("Fetching subscription plans by myService ID: {}", myServiceId);
    List<SubscriptionPlan> plans = subscriptionPlanRepository.findByMyServiceId(myServiceId);
    logger.info("Fetched {} subscription plans for myService ID: {}", plans.size(), myServiceId);
    return plans;
  }

  public void deleteSubscriptionPlan(Long id) {
    logger.info("Deleting subscription plan with ID: {}", id);
    subscriptionPlanRepository.deleteById(id);
    logger.info("Deleted subscription plan successfully with ID: {}", id);
  }

  public SubscriptionPlan updateSubscriptionPlan(Long id, SubscriptionPlanRequestDto updatedSubscriptionPlan) throws ResourceNotFoundException {
    logger.info("Updating subscription plan with ID: {}, data: {}", id, updatedSubscriptionPlan);

    // Validate unique constraints excluding current plan
    validateSubscriptionPlan(updatedSubscriptionPlan, id);

    SubscriptionPlan target = subscriptionPlanRepository.findById(id)
        .orElseThrow(() -> {
            logger.error("SubscriptionPlan not found with ID: {}", id);
            return new ResourceNotFoundException("SubscriptionPlan", "id", id);
        });

    SubscriptionPlan updated = mapper.partialUpdate(updatedSubscriptionPlan, target);
    updated.setMyService(myServiceRepository.findById(updatedSubscriptionPlan.getMyServiceId())
        .orElseThrow(() -> {
            logger.error("MyService not found with ID: {}", updatedSubscriptionPlan.getMyServiceId());
            return new ResourceNotFoundException("MyService", "id", updatedSubscriptionPlan.getMyServiceId());
        }));
    updated.setCurrency(currencyRepository.findById(updatedSubscriptionPlan.getCurrencyId())
        .orElseThrow(() -> {
            logger.error("Currency not found with ID: {}", updatedSubscriptionPlan.getCurrencyId());
            return new ResourceNotFoundException("Currency", "id", updatedSubscriptionPlan.getCurrencyId());
        }));

    SubscriptionPlan savedSubscriptionPlan = subscriptionPlanRepository.save(updated);
    logger.info("Subscription plan updated successfully: {}", savedSubscriptionPlan);
    return savedSubscriptionPlan;
  }
}
