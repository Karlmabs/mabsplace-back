package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.entities.ServiceAccount;
import com.mabsplace.mabsplaceback.domain.repositories.ServiceAccountRepository;
import org.springframework.stereotype.Service;

@Service
public class ServiceAccountService {

  private final ServiceAccountRepository serviceAccountRepository;

  public ServiceAccountService(ServiceAccountRepository serviceAccountRepository) {
    this.serviceAccountRepository = serviceAccountRepository;
  }

  public ServiceAccount createServiceAccount(ServiceAccount serviceAccount) {
    return serviceAccountRepository.save(serviceAccount);
  }

  public ServiceAccount getServiceAccount(Long id) {
    return serviceAccountRepository.findById(id).orElse(null);
  }

  public ServiceAccount updateServiceAccount(ServiceAccount serviceAccount) {
    return serviceAccountRepository.save(serviceAccount);
  }

  public void deleteServiceAccount(Long id) {
    serviceAccountRepository.deleteById(id);
  }
}
