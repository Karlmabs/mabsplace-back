package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.discount.DiscountResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.myService.MyServiceResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Discount;
import com.mabsplace.mabsplaceback.domain.entities.MyService;
import com.mabsplace.mabsplaceback.domain.mappers.DiscountMapper;
import com.mabsplace.mabsplaceback.domain.services.DiscountService;
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

    private final DiscountService discountService;
    private final DiscountMapper mapper;

    public DiscountController(DiscountService discountService, DiscountMapper mapper) {
        this.discountService = discountService;
        this.mapper = mapper;
    }

    @GetMapping("/all")
    public ResponseEntity<List<DiscountResponseDto>> getAllDiscounts() {
        List<Discount> discounts = discountService.getAllDiscounts();
        return new ResponseEntity<>(mapper.toDtoList(discounts), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscountResponseDto> getDiscountById(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toDto(discountService.getDiscount(id)));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<Double> getDiscountByUserId(@PathVariable Long id) {
        return ResponseEntity.ok(discountService.getDiscountForUser(id));
    }
}
