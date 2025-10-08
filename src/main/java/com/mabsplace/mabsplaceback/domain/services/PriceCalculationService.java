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

    public PriceCalculationDto calculatePrice(DigitalProduct product, BigDecimal requestedAmount) {
        logger.info("Calculating price for product: {} with amount: {}", product.getName(), requestedAmount);

        // 1. Get exchange rate
        ExchangeRate exchangeRate = exchangeRateRepository
                .findByFromCurrencyAndToCurrencyAndIsActiveTrue(product.getBaseCurrency(), "XAF")
                .orElseThrow(() -> {
                    logger.error("Exchange rate not found for {} to XAF", product.getBaseCurrency());
                    return new ResourceNotFoundException("ExchangeRate", "fromCurrency-toCurrency",
                            product.getBaseCurrency() + "-XAF");
                });

        logger.info("Found exchange rate: {} {} = 1 XAF", exchangeRate.getRate(), product.getBaseCurrency());

        // 2. Convert price to XAF
        BigDecimal convertedPrice = requestedAmount
                .multiply(exchangeRate.getRate())
                .setScale(2, RoundingMode.HALF_UP);

        logger.info("Converted price: {} XAF", convertedPrice);

        // 3. Calculate service fee based on product category
        ServiceFeeConfig feeConfig = serviceFeeConfigRepository
                .findByProductCategoryAndIsActiveTrue(product.getCategory())
                .orElseThrow(() -> {
                    logger.error("Service fee config not found for category: {}", product.getCategory().getName());
                    return new ResourceNotFoundException("ServiceFeeConfig", "productCategory",
                            product.getCategory().getName());
                });

        BigDecimal serviceFee;
        if (feeConfig.getFeeType() == ServiceFeeConfig.FeeType.PERCENTAGE) {
            serviceFee = convertedPrice
                    .multiply(feeConfig.getFeeValue())
                    .setScale(2, RoundingMode.HALF_UP);
            logger.info("Service fee ({}%): {} XAF", feeConfig.getFeeValue().multiply(BigDecimal.valueOf(100)), serviceFee);
        } else {
            serviceFee = feeConfig.getFeeValue();
            logger.info("Service fee (fixed): {} XAF", serviceFee);
        }

        // 4. Calculate total
        BigDecimal totalAmount = convertedPrice.add(serviceFee);

        logger.info("Total amount: {} XAF", totalAmount);

        // 5. Build breakdown
        String breakdown = String.format(
                "Base amount: %s %s\n" +
                "Exchange rate: 1 %s = %s XAF\n" +
                "Converted price: %s XAF\n" +
                "Service fee (%s%%): %s XAF\n" +
                "Total: %s XAF",
                requestedAmount, product.getBaseCurrency(),
                product.getBaseCurrency(), exchangeRate.getRate(),
                convertedPrice,
                feeConfig.getFeeValue().multiply(BigDecimal.valueOf(100)),
                serviceFee,
                totalAmount
        );

        return PriceCalculationDto.builder()
                .requestedAmount(requestedAmount)
                .baseCurrency(product.getBaseCurrency())
                .baseCurrencyPrice(requestedAmount)
                .exchangeRate(exchangeRate.getRate())
                .convertedPrice(convertedPrice)
                .serviceFee(serviceFee)
                .serviceFeePercentage(feeConfig.getFeeType() == ServiceFeeConfig.FeeType.PERCENTAGE ?
                        feeConfig.getFeeValue().multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .breakdown(breakdown)
                .build();
    }
}
