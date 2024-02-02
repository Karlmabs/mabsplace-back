package com.mabsplace.mabsplaceback.domain.services;


import com.mabsplace.mabsplaceback.domain.dtos.myService.MyServiceRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.MyService;
import com.mabsplace.mabsplaceback.domain.entities.ServiceAccount;
import com.mabsplace.mabsplaceback.domain.entities.SubscriptionPlan;
import com.mabsplace.mabsplaceback.domain.mappers.MyServiceMapper;
import com.mabsplace.mabsplaceback.domain.repositories.MyServiceRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MyServiceService {

  private final MyServiceRepository myServiceRepository;

  private final MyServiceMapper mapper;

  public MyServiceService(MyServiceRepository myServiceRepository, MyServiceMapper mapper) {
    this.myServiceRepository = myServiceRepository;
    this.mapper = mapper;
  }

  public MyService createService(MyService service) {
    return myServiceRepository.save(service);
  }

  public MyService getService(Long id) {
    return myServiceRepository.findById(id).orElse(null);
  }

  public void deleteService(Long id) {
    myServiceRepository.deleteById(id);
  }

  public List<MyService> getAllServices() {
    return myServiceRepository.findAll();
  }

  public MyService updateService(Long id, MyServiceRequestDto updatedUser) {
    MyService myService = myServiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));
    MyService updated = mapper.partialUpdate(updatedUser, myService);
    return myServiceRepository.save(updated);
  }

  public List<SubscriptionPlan> getSubscriptionPlansByServiceId(Long id) {
    return myServiceRepository.getSubscriptionPlansByServiceId(id);
  }

  public List<ServiceAccount> getAvailableServiceAccounts(Long serviceId) {
    MyService myService = myServiceRepository.findById(serviceId).orElseThrow(() -> new ResourceNotFoundException("Service", "id", serviceId));

    return myService.getServiceAccounts();
  }
}
