package com.mabsplace.mabsplaceback.domain.dtos.digitalgoods;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PriceCalculationDto implements Serializable {
    private BigDecimal requestedAmount; // Montant demandé
    private String baseCurrency;
    private BigDecimal baseCurrencyPrice; // Prix en devise originale
    private BigDecimal exchangeRate;
    private BigDecimal convertedPrice; // Prix converti en XAF
    private BigDecimal serviceFee; // Frais de service
    private BigDecimal serviceFeePercentage; // Pourcentage des frais
    private BigDecimal totalAmount; // Total final
    private String breakdown; // Explication détaillée
}
