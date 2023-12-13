package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.serviceAccount.ServiceAccountRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.ServiceAccount;
import com.mabsplace.mabsplaceback.domain.mappers.ServiceAccountMapper;
import com.mabsplace.mabsplaceback.domain.repositories.MyServiceRepository;
import com.mabsplace.mabsplaceback.domain.repositories.ServiceAccountRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceAccountService {

  private final ServiceAccountRepository serviceAccountRepository;
  private final ServiceAccountMapper mapper;
  private final MyServiceRepository myServiceRepository;

  public ServiceAccountService(ServiceAccountRepository serviceAccountRepository, ServiceAccountMapper mapper, MyServiceRepository myServiceRepository) {
    this.serviceAccountRepository = serviceAccountRepository;
    this.mapper = mapper;
    this.myServiceRepository = myServiceRepository;
  }

  public ServiceAccount createServiceAccount(ServiceAccountRequestDto serviceAccount) throws ResourceNotFoundException{
    ServiceAccount newServiceAccount = mapper.toEntity(serviceAccount);
    newServiceAccount.setMyService(myServiceRepository.findById(serviceAccount.getMyServiceId()).orElseThrow(() -> new ResourceNotFoundException("MyService", "id", serviceAccount.getMyServiceId())));
    return serviceAccountRepository.save(newServiceAccount);
  }

  public void deleteServiceAccount(Long id) {
    serviceAccountRepository.deleteById(id);
  }

  public ServiceAccount getServiceAccountById(Long id) throws ResourceNotFoundException {
    return serviceAccountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ServiceAccount", "id", id));
  }

  public List<ServiceAccount> getAllServiceAccounts() {
    return serviceAccountRepository.findAll();
  }

  public ServiceAccount updateServiceAccount(Long id, ServiceAccountRequestDto updatedServiceAccount) throws ResourceNotFoundException{
    ServiceAccount target = serviceAccountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ServiceAccount", "id", id));
    ServiceAccount updated = mapper.partialUpdate(updatedServiceAccount, target);
    updated.setMyService(myServiceRepository.findById(updatedServiceAccount.getMyServiceId()).orElseThrow(() -> new ResourceNotFoundException("MyService", "id", updatedServiceAccount.getMyServiceId())));
    return serviceAccountRepository.save(updated);
  }
}
