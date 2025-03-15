package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.discount.ServiceDiscountDTO;
import com.mabsplace.mabsplaceback.domain.services.SubscriptionDiscountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/service-discounts")
public class ServiceDiscountController {
    private final SubscriptionDiscountService discountService;
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscountController.class);

    @Autowired
    public ServiceDiscountController(SubscriptionDiscountService discountService) {
        this.discountService = discountService;
    }

    @PostMapping
    public ResponseEntity<ServiceDiscountDTO> createDiscount(@RequestBody ServiceDiscountDTO discountDTO) {
        logger.info("Creating service discount with request: {}", discountDTO);
        ServiceDiscountDTO createdDiscount = discountService.createDiscount(discountDTO);
        logger.info("Created service discount: {}", createdDiscount);
        return ResponseEntity.ok(createdDiscount);
    }

    @PostMapping("/update")
    public ResponseEntity<ServiceDiscountDTO> updateDiscount(@RequestBody ServiceDiscountDTO discountDTO) {
        logger.info("Updating service discount with request: {}", discountDTO);
        ServiceDiscountDTO updatedDiscount = discountService.updateDiscount(discountDTO);
        logger.info("Updated service discount: {}", updatedDiscount);
        return ResponseEntity.ok(updatedDiscount);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceDiscountDTO> getDiscount(@PathVariable Long id) {
        logger.info("Fetching service discount with ID: {}", id);
        ServiceDiscountDTO discount = discountService.getDiscountById(id);
        logger.info("Fetched service discount: {}", discount);
        return ResponseEntity.ok(discount);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiscount(@PathVariable Long id) {
        logger.info("Deleting service discount with ID: {}", id);
        discountService.deleteDiscount(id);
        logger.info("Deleted service discount successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Iterable<ServiceDiscountDTO>> getAllDiscounts() {
        logger.info("Fetching all service discounts");
        Iterable<ServiceDiscountDTO> discounts = discountService.getAllDiscounts();
        logger.info("Fetched service discounts successfully");
        return ResponseEntity.ok(discounts);
    }
}
