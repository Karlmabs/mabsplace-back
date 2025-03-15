package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.myService.MyServiceRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.MyService;
import com.mabsplace.mabsplaceback.domain.entities.ServiceAccount;
import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import com.mabsplace.mabsplaceback.domain.mappers.MyServiceMapper;
import com.mabsplace.mabsplaceback.domain.repositories.MyServiceRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MyServiceService {

  private final MyServiceRepository myServiceRepository;

  private final MyServiceMapper mapper;

  private static final Logger logger = LoggerFactory.getLogger(MyServiceService.class);

  public MyServiceService(MyServiceRepository myServiceRepository, MyServiceMapper mapper) {
    this.myServiceRepository = myServiceRepository;
    this.mapper = mapper;
  }

  public MyService createService(MyService service) {
    logger.info("Creating new service: {}", service);
    MyService createdService = myServiceRepository.save(service);
    logger.info("Service created successfully: {}", createdService);
    return createdService;
  }

  public MyService getService(Long id) {
    logger.info("Retrieving service with ID: {}", id);
    MyService service = myServiceRepository.findById(id)
        .orElseThrow(() -> {
            logger.error("Service not found with ID: {}", id);
            return new ResourceNotFoundException("Service", "id", id);
        });
    logger.info("Retrieved service: {}", service);
    return service;
  }

  public void deleteService(Long id) {
    logger.info("Attempting to delete service with ID: {}", id);
    MyService myService = myServiceRepository.findById(id)
        .orElseThrow(() -> {
            logger.error("Service not found with ID: {}", id);
            return new ResourceNotFoundException("Service", "id", id);
        });

    if (!myService.getServiceAccounts().isEmpty() || !myService.getSubscriptionPlans().isEmpty()) {
      logger.warn("Cannot delete service with associated accounts or subscription plans, ID: {}", id);
      throw new IllegalStateException("Cannot delete service with existing associations");
    }

    myServiceRepository.deleteById(id);
    logger.info("Deleted service successfully with ID: {}", id);
  }

  public List<MyService> getAllServices() {
    logger.info("Fetching all services");
    List<MyService> services = myServiceRepository.findAll();
    logger.info("Fetched {} services", services.size());
    return services;
  }

  public MyService updateService(Long id, MyServiceRequestDto updatedUser) {
    logger.info("Updating service with ID: {}, Request: {}", id, updatedUser);
    MyService existingService = myServiceRepository.findById(id)
        .orElseThrow(() -> {
            logger.error("Service not found with ID: {}", id);
            return new ResourceNotFoundException("Service", "id", id);
        });

    MyService updated = mapper.partialUpdate(updatedUser, existingService);
    MyService savedService = myServiceRepository.save(updated);
    logger.info("Updated service successfully: {}", savedService);
    return savedService;
  }

  public List<SubscriptionPlan> getSubscriptionPlansByServiceId(Long id) {
    logger.info("Fetching subscription plans for service ID: {}", id);
    List<SubscriptionPlan> plans = myServiceRepository.getSubscriptionPlansByServiceId(id);
    logger.info("Fetched {} subscription plans for service ID: {}", plans.size(), id);
    return plans;
  }

  public List<ServiceAccount> getAvailableServiceAccounts(Long serviceId) {
    logger.info("Fetching available service accounts for service ID: {}", serviceId);
    MyService myService = myServiceRepository.findById(serviceId)
        .orElseThrow(() -> {
            logger.error("Service not found with ID: {}", serviceId);
            return new ResourceNotFoundException("Service", "id", serviceId);
        });

    List<ServiceAccount> accounts = myService.getServiceAccounts();
    logger.info("Fetched {} service accounts for service ID: {}", accounts.size(), serviceId);
    return accounts;
  }
}
