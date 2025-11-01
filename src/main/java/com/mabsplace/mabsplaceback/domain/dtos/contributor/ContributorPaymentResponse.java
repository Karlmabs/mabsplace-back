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
public class ContributorPaymentResponse {
    private Long id;
    private ConfigBasicInfo config;
    private UserBasicInfo user;
    private BigDecimal amountPaid;
    private String paymentPeriod;
    private BigDecimal netProfitAtTime;
    private String paymentStatus;
    private String coolpayTransactionRef;
    private String coolpayResponse;
    private String failureReason;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConfigBasicInfo {
        private Long id;
        private String amountType;
        private CurrencyInfo currency;
    }

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
