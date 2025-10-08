package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.ServiceFeeConfigDto;
import com.mabsplace.mabsplaceback.domain.services.ServiceFeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-fees")
public class ServiceFeeController {

    private final ServiceFeeService serviceFeeService;
    private static final Logger logger = LoggerFactory.getLogger(ServiceFeeController.class);

    public ServiceFeeController(ServiceFeeService serviceFeeService) {
        this.serviceFeeService = serviceFeeService;
    }

    @PostMapping
    public ResponseEntity<ServiceFeeConfigDto> createServiceFee(@RequestBody ServiceFeeConfigDto serviceFeeDto) {
        logger.info("Creating service fee config: {}", serviceFeeDto.getName());
        ServiceFeeConfigDto created = serviceFeeService.createServiceFee(serviceFeeDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceFeeConfigDto> updateServiceFee(@PathVariable Long id, @RequestBody ServiceFeeConfigDto serviceFeeDto) {
        logger.info("Updating service fee config ID: {}", id);
        ServiceFeeConfigDto updated = serviceFeeService.updateServiceFee(id, serviceFeeDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServiceFee(@PathVariable Long id) {
        logger.info("Deleting service fee config ID: {}", id);
        serviceFeeService.deleteServiceFee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceFeeConfigDto> getServiceFeeById(@PathVariable Long id) {
        logger.info("Fetching service fee config ID: {}", id);
        ServiceFeeConfigDto serviceFee = serviceFeeService.getServiceFeeById(id);
        return ResponseEntity.ok(serviceFee);
    }

    @GetMapping
    public ResponseEntity<List<ServiceFeeConfigDto>> getAllServiceFees() {
        logger.info("Fetching all service fee configs");
        List<ServiceFeeConfigDto> serviceFees = serviceFeeService.getAllServiceFees();
        return ResponseEntity.ok(serviceFees);
    }
}
