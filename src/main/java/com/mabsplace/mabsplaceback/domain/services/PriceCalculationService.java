package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.PriceCalculationDto;
import com.mabsplace.mabsplaceback.domain.entities.DigitalProduct;
import com.mabsplace.mabsplaceback.domain.entities.ExchangeRate;
import com.mabsplace.mabsplaceback.domain.entities.ServiceFeeConfig;
import com.mabsplace.mabsplaceback.domain.repositories.ExchangeRateRepository;
import com.mabsplace.mabsplaceback.domain.repositories.ServiceFeeConfigRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class PriceCalculationService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final ServiceFeeConfigRepository serviceFeeConfigRepository;
    private static final Logger logger = LoggerFactory.getLogger(PriceCalculationService.class);

    public PriceCalculationService(ExchangeRateRepository exchangeRateRepository,
                                    ServiceFeeConfigRepository serviceFeeConfigRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.serviceFeeConfigRepository = serviceFeeConfigRepository;
    }

    public PriceCalculationDto calculatePrice(DigitalProduct product) {
        logger.info("Calculating price for product: {}", product.getName());

        // Get base fixed price
        BigDecimal fixedPrice = product.getFixedPrice();

        if (fixedPrice == null) {
            logger.error("Fixed price not set for product: {}", product.getName());
            throw new IllegalStateException("Product does not have a fixed price set");
        }

        logger.info("Fixed price for product: {} XAF", fixedPrice);

        // Fetch active service fee config for product category
        Optional<ServiceFeeConfig> feeConfigOpt = serviceFeeConfigRepository
                .findByProductCategoryAndIsActiveTrue(product.getCategory());

        BigDecimal serviceFee = BigDecimal.ZERO;
        BigDecimal serviceFeePercentage = BigDecimal.ZERO;

        if (feeConfigOpt.isPresent()) {
            ServiceFeeConfig feeConfig = feeConfigOpt.get();
            logger.info("Found active fee config for category: {}", product.getCategory().getName());

            // Check if fixedPrice is within min/max range (if specified)
            boolean inRange = true;
            if (feeConfig.getMinAmount() != null && fixedPrice.compareTo(feeConfig.getMinAmount()) < 0) {
                logger.info("Price {} is below minimum amount {}", fixedPrice, feeConfig.getMinAmount());
                inRange = false;
            }
            if (feeConfig.getMaxAmount() != null && fixedPrice.compareTo(feeConfig.getMaxAmount()) > 0) {
                logger.info("Price {} is above maximum amount {}", fixedPrice, feeConfig.getMaxAmount());
                inRange = false;
            }

            if (inRange) {
                if (feeConfig.getFeeType() == ServiceFeeConfig.FeeType.PERCENTAGE) {
                    // Calculate percentage fee
                    serviceFeePercentage = feeConfig.getFeeValue(); // e.g., 0.05 for 5%
                    serviceFee = fixedPrice.multiply(serviceFeePercentage)
                            .setScale(2, RoundingMode.HALF_UP);
                    logger.info("Calculated percentage fee: {} XAF ({}%)", serviceFee, serviceFeePercentage.multiply(new BigDecimal("100")));
                } else {
                    // Fixed fee amount
                    serviceFee = feeConfig.getFeeValue()
                            .setScale(2, RoundingMode.HALF_UP);
                    logger.info("Applied fixed fee: {} XAF", serviceFee);
                }
            }
        } else {
            logger.info("No active fee config found for category: {}", product.getCategory().getName());
        }

        // Calculate total amount
        BigDecimal totalAmount = fixedPrice.add(serviceFee);

        // Construct breakdown message
        String breakdown = String.format(
                "Product: %s\n" +
                "Base Price: %s XAF\n" +
                "Service Fee: %s XAF\n" +
                "Total: %s XAF",
                product.getName(),
                fixedPrice,
                serviceFee,
                totalAmount
        );

        logger.info("Price calculation complete - Base: {} XAF, Fee: {} XAF, Total: {} XAF",
                fixedPrice, serviceFee, totalAmount);

        // Return price calculation (all values in XAF)
        return PriceCalculationDto.builder()
                .requestedAmount(fixedPrice)
                .baseCurrency("XAF")
                .baseCurrencyPrice(fixedPrice)
                .exchangeRate(BigDecimal.ONE)
                .convertedPrice(fixedPrice)
                .serviceFee(serviceFee)               // Now calculated from fee config!
                .serviceFeePercentage(serviceFeePercentage)
                .totalAmount(totalAmount)             // Includes fees!
                .breakdown(breakdown)
                .build();
    }
}
