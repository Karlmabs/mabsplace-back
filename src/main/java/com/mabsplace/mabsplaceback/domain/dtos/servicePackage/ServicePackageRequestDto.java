package com.mabsplace.mabsplaceback.domain.dtos.servicePackage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServicePackageRequestDto {
    private String name;
    private String description;
    private String image;
    private Set<Long> serviceIds;
    private boolean active;
}
