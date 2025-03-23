package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.packageSubscriptionPlan.PackageSubscriptionPlanRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.packageSubscriptionPlan.PackageSubscriptionPlanResponseDto;
import com.mabsplace.mabsplaceback.domain.services.PackageSubscriptionPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/package-subscription-plans")
public class PackageSubscriptionPlanController {
    private static final Logger logger = LoggerFactory.getLogger(PackageSubscriptionPlanController.class);
    
    private final PackageSubscriptionPlanService planService;
    
    public PackageSubscriptionPlanController(PackageSubscriptionPlanService planService) {
        this.planService = planService;
    }
    
    @GetMapping
    public ResponseEntity<List<PackageSubscriptionPlanResponseDto>> getAllPlans() {
        logger.info("API request to get all package subscription plans");
        return ResponseEntity.ok(planService.getAllPlans());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PackageSubscriptionPlanResponseDto> getPlanById(@PathVariable Long id) {
        logger.info("API request to get package subscription plan with ID: {}", id);
        return ResponseEntity.ok(planService.getPlanById(id));
    }
    
    @GetMapping("/package/{packageId}")
    public ResponseEntity<List<PackageSubscriptionPlanResponseDto>> getPlansByPackageId(
            @PathVariable Long packageId) {
        logger.info("API request to get subscription plans for package ID: {}", packageId);
        return ResponseEntity.ok(planService.getPlansByPackageId(packageId));
    }
    
    @PostMapping
    public ResponseEntity<PackageSubscriptionPlanResponseDto> createPlan(
            @RequestBody PackageSubscriptionPlanRequestDto dto) {
        logger.info("API request to create package subscription plan: {}", dto.getName());
        return new ResponseEntity<>(planService.createPlan(dto), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PackageSubscriptionPlanResponseDto> updatePlan(
            @PathVariable Long id, 
            @RequestBody PackageSubscriptionPlanRequestDto dto) {
        logger.info("API request to update package subscription plan with ID: {}", id);
        return ResponseEntity.ok(planService.updatePlan(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        logger.info("API request to delete package subscription plan with ID: {}", id);
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}
