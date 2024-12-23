package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.discount.ServiceDiscountDTO;
import com.mabsplace.mabsplaceback.domain.entities.ServiceDiscount;
import com.mabsplace.mabsplaceback.domain.mappers.ServiceDiscountMapper;
import com.mabsplace.mabsplaceback.domain.services.SubscriptionDiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/service-discounts")
public class ServiceDiscountController {
    private final SubscriptionDiscountService discountService;


    @Autowired
    public ServiceDiscountController(SubscriptionDiscountService discountService) {
        this.discountService = discountService;
    }

    @PostMapping
    public ResponseEntity<ServiceDiscountDTO> createDiscount(@RequestBody ServiceDiscountDTO discountDTO) {
        return ResponseEntity.ok(discountService.createDiscount(discountDTO));
    }

    @PostMapping("/update")
    public ResponseEntity<ServiceDiscountDTO> updateDiscount(@RequestBody ServiceDiscountDTO discountDTO) {
        return ResponseEntity.ok(discountService.updateDiscount(discountDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceDiscountDTO> getDiscount(@PathVariable Long id) {
        return ResponseEntity.ok(discountService.getDiscountById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiscount(@PathVariable Long id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Iterable<ServiceDiscountDTO>> getAllDiscounts() {
        return ResponseEntity.ok(discountService.getAllDiscounts());
    }
}
