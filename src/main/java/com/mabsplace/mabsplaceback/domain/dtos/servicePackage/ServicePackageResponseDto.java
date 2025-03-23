package com.mabsplace.mabsplaceback.domain.dtos.servicePackage;

import com.mabsplace.mabsplaceback.domain.dtos.myService.MyServiceResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServicePackageResponseDto implements Serializable {
    private Long id;
    private String name;
    private String description;
    private String image;
    private Set<MyServiceResponseDto> services;
    private boolean active;
}
