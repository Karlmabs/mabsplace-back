package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.discount.DiscountResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Discount;
import com.mabsplace.mabsplaceback.domain.mappers.DiscountMapper;
import com.mabsplace.mabsplaceback.domain.services.DiscountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/discounts")
public class DiscountController {

    private static final Logger logger = LoggerFactory.getLogger(DiscountController.class);
    private final DiscountService discountService;
    private final DiscountMapper mapper;

    public DiscountController(DiscountService discountService, DiscountMapper mapper) {
        this.discountService = discountService;
        this.mapper = mapper;
    }

    @GetMapping("/all")
    public ResponseEntity<List<DiscountResponseDto>> getAllDiscounts() {
        logger.info("Fetching all discounts");
        List<Discount> discounts = discountService.getAllDiscounts();
        logger.info("Fetched {} discounts", discounts.size());
        return new ResponseEntity<>(mapper.toDtoList(discounts), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscountResponseDto> getDiscountById(@PathVariable Long id) {
        logger.info("Fetching discount with ID: {}", id);
        Discount discount = discountService.getDiscount(id);
        logger.info("Fetched discount: {}", discount);
        return ResponseEntity.ok(mapper.toDto(discount));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<Double> getDiscountByUserId(@PathVariable Long id) {
        logger.info("Fetching discount for user ID: {}", id);
        Double discountValue = discountService.getDiscountForUser(id);
        logger.info("Fetched discount value for user {}: {}", id, discountValue);
        return ResponseEntity.ok(discountValue);
    }
}
