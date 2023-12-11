package com.mabsplace.mabsplaceback.domain.dtos.role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

//@Value
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RoleResponseDto implements Serializable {

  private Long id;
  private String name;
  private String code;
  private String description;

}
