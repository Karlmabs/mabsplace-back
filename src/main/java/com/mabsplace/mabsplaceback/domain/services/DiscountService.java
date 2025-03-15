package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.entities.Discount;
import com.mabsplace.mabsplaceback.domain.repositories.DiscountRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class DiscountService {

    private final DiscountRepository discountRepository;
    private static final Logger logger = LoggerFactory.getLogger(DiscountService.class);

    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    public List<Discount> getAllDiscounts() {
        logger.info("Fetching all discounts from repository");
        List<Discount> discounts = discountRepository.findAll();
        logger.info("Fetched {} discounts", discounts.size());
        return discounts;
    }

    public Discount getDiscount(Long id) {
        logger.info("Fetching discount by ID: {}", id);
        Optional<Discount> discount = discountRepository.findById(id);
        if (discount.isPresent()) {
            logger.info("Fetched discount: {}", discount.get());
            return discount.get();
        } else {
            logger.warn("Discount not found with ID: {}", id);
            return null;
        }
    }

    public double getDiscountForUser(Long userId) {
        logger.info("Fetching discount for user ID: {}", userId);
        Optional<Discount> discount = discountRepository.findByUserId(userId);
        if (discount.isPresent()) {
            logger.info("Fetched discount amount: {} for user ID: {}", discount.get().getAmount(), userId);
            return discount.get().getAmount();
        } else {
            logger.info("No discount found for user ID: {}", userId);
            return 0.0;
        }
    }
}
