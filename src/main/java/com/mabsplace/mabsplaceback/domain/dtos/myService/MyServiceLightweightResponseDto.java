package com.mabsplace.mabsplaceback.domain.dtos.myService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MyServiceLightweightResponseDto implements Serializable {
    private Long id;
    private String name;
    private String logo;
    private String image;
    private String description;
    private Integer subscriptionPlanCount;
    private Integer serviceAccountCount;
    private List<String> planNames;
}
