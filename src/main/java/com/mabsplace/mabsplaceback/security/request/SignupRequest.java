package com.mabsplace.mabsplaceback.security.request;

import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest implements Serializable {

  private String username;

  private String email;

  private String profileName;

  private Set<String> role;

  private String password;

  private String phonenumber;

  private String firstname;

  private String lastname;

  private String promoCode;

  private String referralCode;

}
