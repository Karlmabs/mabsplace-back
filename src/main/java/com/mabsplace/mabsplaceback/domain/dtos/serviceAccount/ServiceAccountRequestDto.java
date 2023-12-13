package com.mabsplace.mabsplaceback.domain.dtos.serviceAccount;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceAccountRequestDto implements Serializable {
  private String login;

  private String password;

  private String accountDetails;

  private long myServiceId;
}
