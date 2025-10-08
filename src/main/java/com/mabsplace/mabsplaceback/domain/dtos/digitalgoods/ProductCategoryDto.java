package com.mabsplace.mabsplaceback.domain.dtos.digitalgoods;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductCategoryDto implements Serializable {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private Boolean active;
    private Date createdAt;
    private Date updatedAt;
}
