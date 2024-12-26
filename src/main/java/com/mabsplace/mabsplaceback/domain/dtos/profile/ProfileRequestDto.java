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
public class ProfileRequestDto implements Serializable {
  private long serviceAccountId;
  private long subscriptionId;
  private String pin;
  private String profileName;
  private ProfileStatus status;
}
