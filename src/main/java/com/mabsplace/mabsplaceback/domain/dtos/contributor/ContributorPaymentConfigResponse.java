package com.mabsplace.mabsplaceback.domain.dtos.contributor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContributorPaymentConfigResponse {
    private Long id;
    private UserBasicInfo user;
    private Boolean isActive;
    private String amountType;
    private BigDecimal fixedAmount;
    private BigDecimal percentageValue;
    private BigDecimal minProfitThreshold;
    private Boolean useGlobalThreshold;
    private Boolean alwaysPay;
    private CurrencyInfo currency;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserBasicInfo {
        private Long id;
        private String username;
        private String email;
        private String firstname;
        private String lastname;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CurrencyInfo {
        private Long id;
        private String name;
        private String symbol;
    }
}
