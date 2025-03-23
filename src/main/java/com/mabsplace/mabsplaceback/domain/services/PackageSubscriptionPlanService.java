package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.packageSubscriptionPlan.PackageSubscriptionPlanRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.packageSubscriptionPlan.PackageSubscriptionPlanResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Currency;
import com.mabsplace.mabsplaceback.domain.entities.PackageSubscriptionPlan;
import com.mabsplace.mabsplaceback.domain.entities.ServicePackage;
import com.mabsplace.mabsplaceback.domain.mappers.PackageSubscriptionPlanMapper;
import com.mabsplace.mabsplaceback.domain.repositories.CurrencyRepository;
import com.mabsplace.mabsplaceback.domain.repositories.PackageSubscriptionPlanRepository;
import com.mabsplace.mabsplaceback.domain.repositories.ServicePackageRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PackageSubscriptionPlanService {
    private static final Logger logger = LoggerFactory.getLogger(PackageSubscriptionPlanService.class);
    
    private final PackageSubscriptionPlanRepository planRepository;
    private final ServicePackageRepository packageRepository;
    private final CurrencyRepository currencyRepository;
    private final PackageSubscriptionPlanMapper planMapper;
    
    public PackageSubscriptionPlanService(PackageSubscriptionPlanRepository planRepository,
                                         ServicePackageRepository packageRepository,
                                         CurrencyRepository currencyRepository,
                                         PackageSubscriptionPlanMapper planMapper) {
        this.planRepository = planRepository;
        this.packageRepository = packageRepository;
        this.currencyRepository = currencyRepository;
        this.planMapper = planMapper;
    }
    
    /**
     * Get all package subscription plans
     */
    public List<PackageSubscriptionPlanResponseDto> getAllPlans() {
        logger.info("Fetching all package subscription plans");
        return planMapper.toDtoList(planRepository.findAll());
    }
    
    /**
     * Get a package subscription plan by ID
     */
    public PackageSubscriptionPlanResponseDto getPlanById(Long id) {
        logger.info("Fetching package subscription plan with ID: {}", id);
        PackageSubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PackageSubscriptionPlan", "id", id));
        return planMapper.toDto(plan);
    }
    
    /**
     * Get all active subscription plans for a package
     */
    public List<PackageSubscriptionPlanResponseDto> getPlansByPackageId(Long packageId) {
        logger.info("Fetching active subscription plans for package ID: {}", packageId);
        return planMapper.toDtoList(planRepository.findByServicePackageIdAndActiveTrue(packageId));
    }
    
    /**
     * Create a new package subscription plan
     */
    @Transactional
    public PackageSubscriptionPlanResponseDto createPlan(PackageSubscriptionPlanRequestDto dto) {
        logger.info("Creating new package subscription plan: {}", dto.getName());
        PackageSubscriptionPlan plan = planMapper.toEntity(dto);
        
        // Set currency
        Currency currency = currencyRepository.findById(dto.getCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", "id", dto.getCurrencyId()));
        plan.setCurrency(currency);
        
        // Set service package
        ServicePackage servicePackage = packageRepository.findById(dto.getPackageId())
                .orElseThrow(() -> new ResourceNotFoundException("ServicePackage", "id", dto.getPackageId()));
        plan.setServicePackage(servicePackage);
        
        PackageSubscriptionPlan savedPlan = planRepository.save(plan);
        logger.info("Package subscription plan created successfully with ID: {}", savedPlan.getId());
        return planMapper.toDto(savedPlan);
    }
    
    /**
     * Update a package subscription plan
     */
    @Transactional
    public PackageSubscriptionPlanResponseDto updatePlan(Long id, PackageSubscriptionPlanRequestDto dto) {
        logger.info("Updating package subscription plan with ID: {}", id);
        PackageSubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PackageSubscriptionPlan", "id", id));
        
        // Update fields
        planMapper.partialUpdate(dto, plan);
        
        // Update currency if provided
        if (dto.getCurrencyId() != null) {
            Currency currency = currencyRepository.findById(dto.getCurrencyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Currency", "id", dto.getCurrencyId()));
            plan.setCurrency(currency);
        }
        
        // Update service package if provided
        if (dto.getPackageId() != null) {
            ServicePackage servicePackage = packageRepository.findById(dto.getPackageId())
                    .orElseThrow(() -> new ResourceNotFoundException("ServicePackage", "id", dto.getPackageId()));
            plan.setServicePackage(servicePackage);
        }
        
        PackageSubscriptionPlan updatedPlan = planRepository.save(plan);
        logger.info("Package subscription plan updated successfully: {}", updatedPlan.getId());
        return planMapper.toDto(updatedPlan);
    }
    
    /**
     * Delete a package subscription plan
     */
    @Transactional
    public void deletePlan(Long id) {
        logger.info("Deleting package subscription plan with ID: {}", id);
        if (!planRepository.existsById(id)) {
            throw new ResourceNotFoundException("PackageSubscriptionPlan", "id", id);
        }
        planRepository.deleteById(id);
        logger.info("Package subscription plan deleted successfully with ID: {}", id);
    }
}
