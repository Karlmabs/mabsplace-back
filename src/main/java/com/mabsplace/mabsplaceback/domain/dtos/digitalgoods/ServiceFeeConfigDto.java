package com.mabsplace.mabsplaceback.domain.dtos.digitalgoods;

import com.mabsplace.mabsplaceback.domain.entities.ServiceFeeConfig;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ServiceFeeConfigDto implements Serializable {
    private Long id;
    private String name;
    private Long productCategoryId;
    private String productCategoryName;
    private ServiceFeeConfig.FeeType feeType;
    private BigDecimal feeValue;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Boolean isActive;
    private Date createdAt;
    private Date updatedAt;
}
