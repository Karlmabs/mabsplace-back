package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.serviceAccount.ServiceAccountRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.serviceAccount.ServiceAccountResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.ServiceAccount;
import com.mabsplace.mabsplaceback.domain.mappers.ServiceAccountMapper;
import com.mabsplace.mabsplaceback.domain.services.ServiceAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-accounts")
public class ServiceAccountController {
  
  private final ServiceAccountService serviceAccountService;
  private final ServiceAccountMapper mapper;

  public ServiceAccountController(ServiceAccountService serviceAccountService, ServiceAccountMapper mapper) {
    this.serviceAccountService = serviceAccountService;
    this.mapper = mapper;
  }

  @PostMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<ServiceAccountResponseDto> createServiceAccount(@RequestBody ServiceAccountRequestDto serviceAccountRequestDto) {
    ServiceAccount createdServiceAccount = serviceAccountService.createServiceAccount(serviceAccountRequestDto);
    return new ResponseEntity<>(mapper.toDto(createdServiceAccount), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<ServiceAccountResponseDto> getServiceAccountById(@PathVariable Long id) {
    return ResponseEntity.ok(mapper.toDto(serviceAccountService.getServiceAccountById(id)));
  }

  @GetMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<List<ServiceAccountResponseDto>> getAllServiceAccounts() {
    List<ServiceAccount> ServiceAccounts = serviceAccountService.getAllServiceAccounts();
    return new ResponseEntity<>(mapper.toDtoList(ServiceAccounts), HttpStatus.OK);
  }

  @PutMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<ServiceAccountResponseDto> updateServiceAccount(@PathVariable Long id, @RequestBody ServiceAccountRequestDto updatedServiceAccount) {
    ServiceAccount updated = serviceAccountService.updateServiceAccount(id, updatedServiceAccount);
    if (updated != null) {
      return new ResponseEntity<>(mapper.toDto(updated), HttpStatus.OK);
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @DeleteMapping("/{id}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<Void> deleteServiceAccount(@PathVariable Long id) {
    serviceAccountService.deleteServiceAccount(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
