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

        // NOUVEAU SYSTÈME: Prix fixe en XAF, pas de conversion nécessaire
        BigDecimal fixedPrice = product.getFixedPrice();

        if (fixedPrice == null) {
            logger.error("Fixed price not set for product: {}", product.getName());
            throw new IllegalStateException("Product does not have a fixed price set");
        }

        logger.info("Fixed price for product: {} XAF", fixedPrice);

        // Construct breakdown message
        String breakdown = String.format(
                "Product: %s\n" +
                "Price: %s XAF\n" +
                "Total: %s XAF",
                product.getName(),
                fixedPrice,
                fixedPrice
        );

        // Return price calculation (all values in XAF)
        return PriceCalculationDto.builder()
                .requestedAmount(fixedPrice)  // Montant = prix fixe
                .baseCurrency("XAF")          // Toujours en XAF maintenant
                .baseCurrencyPrice(fixedPrice)
                .exchangeRate(BigDecimal.ONE) // Pas de conversion
                .convertedPrice(fixedPrice)
                .serviceFee(BigDecimal.ZERO)  // Frais déjà inclus dans fixedPrice
                .serviceFeePercentage(BigDecimal.ZERO)
                .totalAmount(fixedPrice)
                .breakdown(breakdown)
                .build();
    }
}
