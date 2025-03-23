package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.servicePackage.ServicePackageRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.servicePackage.ServicePackageResponseDto;
import com.mabsplace.mabsplaceback.domain.services.ServicePackageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-packages")
public class ServicePackageController {
    private static final Logger logger = LoggerFactory.getLogger(ServicePackageController.class);
    
    private final ServicePackageService packageService;
    
    public ServicePackageController(ServicePackageService packageService) {
        this.packageService = packageService;
    }
    
    @GetMapping
    public ResponseEntity<List<ServicePackageResponseDto>> getAllPackages() {
        logger.info("API request to get all service packages");
        return ResponseEntity.ok(packageService.getAllPackages());
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<ServicePackageResponseDto>> getActivePackages() {
        logger.info("API request to get active service packages");
        return ResponseEntity.ok(packageService.getActivePackages());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ServicePackageResponseDto> getPackageById(@PathVariable Long id) {
        logger.info("API request to get service package with ID: {}", id);
        return ResponseEntity.ok(packageService.getPackageById(id));
    }
    
    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<ServicePackageResponseDto>> getPackagesByServiceId(@PathVariable Long serviceId) {
        logger.info("API request to get packages by service ID: {}", serviceId);
        return ResponseEntity.ok(packageService.getPackagesByServiceId(serviceId));
    }
    
    @PostMapping
    public ResponseEntity<ServicePackageResponseDto> createPackage(@RequestBody ServicePackageRequestDto dto) {
        logger.info("API request to create service package: {}", dto.getName());
        return new ResponseEntity<>(packageService.createPackage(dto), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ServicePackageResponseDto> updatePackage(
            @PathVariable Long id, 
            @RequestBody ServicePackageRequestDto dto) {
        logger.info("API request to update service package with ID: {}", id);
        return ResponseEntity.ok(packageService.updatePackage(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        logger.info("API request to delete service package with ID: {}", id);
        packageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }
}
