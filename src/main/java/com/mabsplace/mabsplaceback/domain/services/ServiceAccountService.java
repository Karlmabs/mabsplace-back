package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.serviceAccount.ServiceAccountRequestDto;
import com.mabsplace.mabsplaceback.domain.entities.ServiceAccount;
import com.mabsplace.mabsplaceback.domain.mappers.ServiceAccountMapper;
import com.mabsplace.mabsplaceback.domain.repositories.MyServiceRepository;
import com.mabsplace.mabsplaceback.domain.repositories.ServiceAccountRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ServiceAccountService {
    private static final Logger logger = LoggerFactory.getLogger(ServiceAccountService.class);
    private final ServiceAccountRepository serviceAccountRepository;
    private final ServiceAccountMapper mapper;
    private final MyServiceRepository myServiceRepository;

    public ServiceAccountService(ServiceAccountRepository serviceAccountRepository, ServiceAccountMapper mapper, MyServiceRepository myServiceRepository) {
        this.serviceAccountRepository = serviceAccountRepository;
        this.mapper = mapper;
        this.myServiceRepository = myServiceRepository;
    }

    public ServiceAccount createServiceAccount(ServiceAccountRequestDto serviceAccount) throws ResourceNotFoundException {
        logger.info("Creating service account with data: {}", serviceAccount);
        List<ServiceAccount> existingServiceAccounts = serviceAccountRepository.findByMyServiceId(serviceAccount.getMyServiceId());
        boolean emailExists = existingServiceAccounts.stream()
                .anyMatch(sa -> sa.getLogin().equalsIgnoreCase(serviceAccount.getLogin()));

        if (emailExists) {
            logger.warn("Service account creation failed: login already exists for myServiceId {}", serviceAccount.getMyServiceId());
            throw new IllegalStateException("A service account with the same login already exists under this service.");
        }
        ServiceAccount newServiceAccount = mapper.toEntity(serviceAccount);

        newServiceAccount.setMyService(myServiceRepository.findById(serviceAccount.getMyServiceId())
                .orElseThrow(() -> {
                    logger.error("MyService not found with ID: {}", serviceAccount.getMyServiceId());
                    return new ResourceNotFoundException("MyService", "id", serviceAccount.getMyServiceId());
                }));

        ServiceAccount savedServiceAccount = serviceAccountRepository.save(newServiceAccount);
        logger.info("Service account created successfully: {}", savedServiceAccount);
        return savedServiceAccount;
    }

    public void deleteServiceAccount(Long id) {
        logger.info("Deleting service account with ID: {}", id);
        serviceAccountRepository.deleteById(id);
        logger.info("Service account deleted successfully with ID: {}", id);
    }

    public ServiceAccount getServiceAccountById(Long id) throws ResourceNotFoundException {
        logger.info("Retrieving service account with ID: {}", id);
        ServiceAccount account = serviceAccountRepository.findById(id).orElseThrow(() -> {
            logger.error("Service account not found with ID: {}", id);
            return new ResourceNotFoundException("ServiceAccount", "id", id);
        });
        logger.info("Retrieved service account successfully: {}", account);
        return account;
    }

    public List<ServiceAccount> getAllServiceAccounts() {
        logger.info("Fetching all service accounts");
        List<ServiceAccount> accounts = serviceAccountRepository.findAll();
        logger.info("Fetched {} service accounts", accounts.size());
        return accounts;
    }

    public List<ServiceAccount> getServiceAccountsByMyServiceId(Long myServiceId) throws ResourceNotFoundException {
        logger.info("Fetching service accounts by myService ID: {}", myServiceId);
        List<ServiceAccount> accounts = serviceAccountRepository.findByMyServiceId(myServiceId);
        logger.info("Fetched {} service accounts for myService ID: {}", accounts.size(), myServiceId);
        return accounts;
    }

    public ServiceAccount updateServiceAccount(Long id, ServiceAccountRequestDto updatedServiceAccount) throws ResourceNotFoundException {
        logger.info("Updating service account with ID: {}, data: {}", id, updatedServiceAccount);
        ServiceAccount target = serviceAccountRepository.findById(id).orElseThrow(() -> {
            logger.error("ServiceAccount not found with ID: {}", id);
            return new ResourceNotFoundException("ServiceAccount", "id", id);
        });

        List<ServiceAccount> existingServiceAccounts = serviceAccountRepository.findByMyServiceId(updatedServiceAccount.getMyServiceId())
                .stream().filter(sa -> !sa.getId().equals(id)).toList();
        boolean emailExists = existingServiceAccounts.stream()
                .anyMatch(sa -> sa.getLogin().equalsIgnoreCase(updatedServiceAccount.getLogin()));
        if (emailExists) {
            logger.warn("Service account update failed: login already exists for myServiceId {}", updatedServiceAccount.getMyServiceId());
            throw new IllegalStateException("A service account with the same login already exists under this service.");
        }
        ServiceAccount updated = mapper.partialUpdate(updatedServiceAccount, target);
        updated.setMyService(myServiceRepository.findById(updatedServiceAccount.getMyServiceId()).orElseThrow(() -> {
            logger.error("MyService not found with ID: {}", updatedServiceAccount.getMyServiceId());
            return new ResourceNotFoundException("MyService", "id", updatedServiceAccount.getMyServiceId());
        }));

        ServiceAccount account = serviceAccountRepository.save(updated);
        logger.info("Service account updated successfully: {}", account);
        return account;
    }


}
