package com.mabsplace.mabsplaceback.domain.dtos.digitalgoods;

import com.mabsplace.mabsplaceback.domain.entities.DigitalGoodsOrder;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DigitalGoodsOrderDto implements Serializable {
    private Long id;
    private Long userId;
    private String username;
    private Long productId;
    private String productName;
    private BigDecimal amount;
    private String baseCurrency;
    private BigDecimal baseCurrencyPrice;
    private BigDecimal exchangeRate;
    private BigDecimal convertedPrice;
    private BigDecimal serviceFee;
    private BigDecimal totalAmount;
    private BigDecimal profit;
    private DigitalGoodsOrder.OrderStatus orderStatus;
    private String paymentMethod;
    private Long transactionId;
    private String deliveryInfo;
    private String adminNotes;
    private Long deliveredBy;
    private String deliveredByUsername;
    private Map<String, String> customerInputData; // Nouveau: Données client
    private String productSnapshot;                // Nouveau: État du produit
    private Date createdAt;
    private Date updatedAt;
    private Date paidAt;
    private Date deliveredAt;
}
