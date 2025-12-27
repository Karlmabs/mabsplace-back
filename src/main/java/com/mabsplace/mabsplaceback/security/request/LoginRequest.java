package com.mabsplace.mabsplaceback.security.request;


import lombok.Getter;

@Getter
public class LoginRequest {
  private String username;

  private String password;

  public void setUsername(String username) {
    this.username = (username != null) ? username.trim().toLowerCase() : null;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
