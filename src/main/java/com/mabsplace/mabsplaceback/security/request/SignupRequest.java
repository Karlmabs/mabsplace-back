package com.mabsplace.mabsplaceback.security.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest implements Serializable {

  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  @Pattern(regexp = "^[a-z0-9_-]+$", message = "Username must contain only lowercase letters, numbers, underscores, and hyphens")
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
