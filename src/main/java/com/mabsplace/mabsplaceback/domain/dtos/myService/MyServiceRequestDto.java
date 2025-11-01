package com.mabsplace.mabsplaceback.domain.dtos.myService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MyServiceRequestDto implements Serializable {

  private String name;
  private String logo;
  private String image;
  private String description;
  private Boolean showAccountCredentials;
  private Boolean showProfileName;
  private Boolean showProfilePin;

}
