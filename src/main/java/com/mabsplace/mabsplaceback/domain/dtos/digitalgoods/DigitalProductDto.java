package com.mabsplace.mabsplaceback.domain.dtos.digitalgoods;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DigitalProductDto implements Serializable {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private String imageUrl;
    private String baseCurrency;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Boolean isActive;
    private Date createdAt;
    private Date updatedAt;
}
