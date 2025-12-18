package com.mabsplace.mabsplaceback.domain.dtos.digitalgoods;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
    private BigDecimal fixedPrice;           // Nouveau: Prix fixe en XAF
    private BigDecimal profitMargin;         // Nouveau: Marge bénéficiaire
    private List<CustomInputFieldDto> customInputFields; // Nouveau: Champs personnalisés
    private Boolean isActive;
    private Date createdAt;
    private Date updatedAt;
}
