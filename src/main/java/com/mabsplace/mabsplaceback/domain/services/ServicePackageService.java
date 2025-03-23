package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.servicePackage.ServicePackageRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.servicePackage.ServicePackageResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.MyService;
import com.mabsplace.mabsplaceback.domain.entities.ServicePackage;
import com.mabsplace.mabsplaceback.domain.mappers.ServicePackageMapper;
import com.mabsplace.mabsplaceback.domain.repositories.MyServiceRepository;
import com.mabsplace.mabsplaceback.domain.repositories.ServicePackageRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ServicePackageService {
    private static final Logger logger = LoggerFactory.getLogger(ServicePackageService.class);
    
    private final ServicePackageRepository packageRepository;
    private final MyServiceRepository serviceRepository;
    private final ServicePackageMapper packageMapper;
    
    public ServicePackageService(ServicePackageRepository packageRepository, 
                                MyServiceRepository serviceRepository,
                                ServicePackageMapper packageMapper) {
        this.packageRepository = packageRepository;
        this.serviceRepository = serviceRepository;
        this.packageMapper = packageMapper;
    }
    
    /**
     * Get all service packages
     */
    public List<ServicePackageResponseDto> getAllPackages() {
        logger.info("Fetching all service packages");
        return packageMapper.toDtoList(packageRepository.findAll());
    }
    
    /**
     * Get all active service packages
     */
    public List<ServicePackageResponseDto> getActivePackages() {
        logger.info("Fetching active service packages");
        return packageMapper.toDtoList(packageRepository.findByActiveTrue());
    }
    
    /**
     * Get a service package by ID
     */
    public ServicePackageResponseDto getPackageById(Long id) {
        logger.info("Fetching service package with ID: {}", id);
        ServicePackage servicePackage = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServicePackage", "id", id));
        return packageMapper.toDto(servicePackage);
    }
    
    /**
     * Create a new service package
     */
    @Transactional
    public ServicePackageResponseDto createPackage(ServicePackageRequestDto dto) {
        logger.info("Creating new service package: {}", dto.getName());
        ServicePackage servicePackage = packageMapper.toEntity(dto);
        
        // Set services
        Set<MyService> services = new HashSet<>();
        if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty()) {
            for (Long serviceId : dto.getServiceIds()) {
                MyService service = serviceRepository.findById(serviceId)
                        .orElseThrow(() -> new ResourceNotFoundException("MyService", "id", serviceId));
                services.add(service);
            }
        }
        servicePackage.setServices(services);
        
        ServicePackage savedPackage = packageRepository.save(servicePackage);
        logger.info("Service package created successfully with ID: {}", savedPackage.getId());
        return packageMapper.toDto(savedPackage);
    }
    
    /**
     * Update a service package
     */
    @Transactional
    public ServicePackageResponseDto updatePackage(Long id, ServicePackageRequestDto dto) {
        logger.info("Updating service package with ID: {}", id);
        ServicePackage servicePackage = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServicePackage", "id", id));
        
        // Update fields
        packageMapper.partialUpdate(dto, servicePackage);
        
        // Update services if provided
        if (dto.getServiceIds() != null) {
            Set<MyService> services = new HashSet<>();
            for (Long serviceId : dto.getServiceIds()) {
                MyService service = serviceRepository.findById(serviceId)
                        .orElseThrow(() -> new ResourceNotFoundException("MyService", "id", serviceId));
                services.add(service);
            }
            servicePackage.setServices(services);
        }
        
        ServicePackage updatedPackage = packageRepository.save(servicePackage);
        logger.info("Service package updated successfully: {}", updatedPackage.getId());
        return packageMapper.toDto(updatedPackage);
    }
    
    /**
     * Delete a service package
     */
    @Transactional
    public void deletePackage(Long id) {
        logger.info("Deleting service package with ID: {}", id);
        if (!packageRepository.existsById(id)) {
            throw new ResourceNotFoundException("ServicePackage", "id", id);
        }
        packageRepository.deleteById(id);
        logger.info("Service package deleted successfully with ID: {}", id);
    }
    
    /**
     * Find packages that include a specific service
     */
    public List<ServicePackageResponseDto> getPackagesByServiceId(Long serviceId) {
        logger.info("Finding packages that include service ID: {}", serviceId);
        List<ServicePackage> packages = packageRepository.findActivePackagesByServiceId(serviceId);
        return packageMapper.toDtoList(packages);
    }
}
