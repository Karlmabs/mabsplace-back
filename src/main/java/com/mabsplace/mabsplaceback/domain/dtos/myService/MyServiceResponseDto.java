package com.mabsplace.mabsplaceback.domain.dtos.myService;

import com.mabsplace.mabsplaceback.domain.dtos.serviceAccount.ServiceAccountResponseDto;
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
public class MyServiceResponseDto implements Serializable {
  private Long id;
  private String name;
  private String description;
  private List<ServiceAccountResponseDto> serviceAccounts;
}
