package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.serviceAccount.ServiceAccountRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.serviceAccount.ServiceAccountResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.ServiceAccount;
import com.mabsplace.mabsplaceback.domain.mappers.ServiceAccountMapper;
import com.mabsplace.mabsplaceback.domain.services.ServiceAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-accounts")
public class ServiceAccountController {

    private final ServiceAccountService serviceAccountService;
    private final ServiceAccountMapper mapper;
    private static final Logger logger = LoggerFactory.getLogger(ServiceAccountController.class);

    public ServiceAccountController(ServiceAccountService serviceAccountService, ServiceAccountMapper mapper) {
        this.serviceAccountService = serviceAccountService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ServiceAccountResponseDto> createServiceAccount(@RequestBody ServiceAccountRequestDto serviceAccountRequestDto) {
        logger.info("Creating service account with request: {}", serviceAccountRequestDto);
        ServiceAccount createdServiceAccount = serviceAccountService.createServiceAccount(serviceAccountRequestDto);
        logger.info("Created service account: {}", createdServiceAccount);
        return new ResponseEntity<>(mapper.toDto(createdServiceAccount), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceAccountResponseDto> getServiceAccountById(@PathVariable Long id) {
        logger.info("Fetching service account with ID: {}", id);
        ServiceAccount serviceAccount = serviceAccountService.getServiceAccountById(id);
        logger.info("Fetched service account: {}", serviceAccount);
        return ResponseEntity.ok(mapper.toDto(serviceAccount));
    }

    @GetMapping
    public ResponseEntity<List<ServiceAccountResponseDto>> getAllServiceAccounts() {
        logger.info("Fetching all service accounts");
        List<ServiceAccount> serviceAccounts = serviceAccountService.getAllServiceAccounts();
        logger.info("Fetched {} service accounts", serviceAccounts.size());
        return new ResponseEntity<>(mapper.toDtoList(serviceAccounts), HttpStatus.OK);
    }

    @GetMapping("/my-service/{myServiceId}")
    public ResponseEntity<List<ServiceAccountResponseDto>> getServiceAccountsByMyServiceId(@PathVariable Long myServiceId) {
        logger.info("Fetching service accounts by myService ID: {}", myServiceId);
        List<ServiceAccount> serviceAccounts = serviceAccountService.getServiceAccountsByMyServiceId(myServiceId);
        logger.info("Fetched {} service accounts for myService ID: {}", serviceAccounts.size(), myServiceId);
        return new ResponseEntity<>(mapper.toDtoList(serviceAccounts), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceAccountResponseDto> updateServiceAccount(@PathVariable Long id, @RequestBody ServiceAccountRequestDto updatedServiceAccount) {
        logger.info("Updating service account with ID: {}, Request: {}", id, updatedServiceAccount);
        ServiceAccount updated = serviceAccountService.updateServiceAccount(id, updatedServiceAccount);
        if (updated != null) {
            logger.info("Updated service account successfully: {}", updated);
            return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
        }
        logger.warn("Service account not found with ID: {}", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServiceAccount(@PathVariable Long id) {
        logger.info("Deleting service account with ID: {}", id);
        serviceAccountService.deleteServiceAccount(id);
        logger.info("Deleted service account successfully with ID: {}", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
