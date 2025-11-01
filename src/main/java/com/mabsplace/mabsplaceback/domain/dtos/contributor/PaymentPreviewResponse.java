package com.mabsplace.mabsplaceback.domain.dtos.contributor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPreviewResponse {

    private BigDecimal currentNetProfit;
    private String paymentPeriod;
    private List<EligiblePayment> eligiblePayments;
    private BigDecimal totalPayoutAmount;
    private Integer totalEligibleContributors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EligiblePayment {
        private Long configId;
        private Long userId;
        private String username;
        private String email;
        private String phoneNumber;
        private String amountType;
        private BigDecimal calculatedAmount;
        private String reason; // e.g., "5% of net profit" or "Fixed amount"
    }
}
