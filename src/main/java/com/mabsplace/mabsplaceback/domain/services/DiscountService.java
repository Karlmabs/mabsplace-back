package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.entities.Discount;
import com.mabsplace.mabsplaceback.domain.repositories.DiscountRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class DiscountService {

    private final DiscountRepository discountRepository;

    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    public List<Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }

    public Discount getDiscount(Long id) {
        return discountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Discount", "id", id));
    }

    public double getDiscountForUser(Long userId) {
        Optional<Discount> discount = discountRepository.findByUserId(userId);
        return discount.map(Discount::getAmount).orElse(0.0);
    }
}
