package com.mabsplace.mabsplaceback.domain.dtos.profile;

import com.mabsplace.mabsplaceback.domain.enums.ProfileStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProfileResponseDto implements Serializable {
  private Long id;
  private long serviceAccountId;
  private String serviceName;
  private String account;
  private long subscriptionId;
  private ProfileStatus status;
  private String pin;
  private String profileName;
}
