package com.mabsplace.mabsplaceback.domain.dtos.user;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserRequestDto implements Serializable {
    private String username;
    private String email;
    private String phonenumber;
    private String firstname;
    private String lastname;
    private String password;
    private String contact;
    private String profileName;
    private String referralCode;
}
