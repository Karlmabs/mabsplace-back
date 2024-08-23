package com.mabsplace.mabsplaceback.domain.dtos.serviceAccount;

import com.mabsplace.mabsplaceback.domain.dtos.profile.ProfileResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceAccountResponseDto implements Serializable {
  private Long id;
  private String login;
  private String password;
  private String accountDetails;
  private long myServiceId;
  private String serviceName;
  private Date paymentDate;
  private List<ProfileResponseDto> profiles;
}
