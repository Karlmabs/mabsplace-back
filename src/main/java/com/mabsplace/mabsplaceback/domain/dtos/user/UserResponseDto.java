package com.mabsplace.mabsplaceback.domain.dtos.user;

import com.mabsplace.mabsplaceback.domain.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserResponseDto implements Serializable{

    private Long id;
    private String username;
    private String email;
    private String phonenumber;
    private String firstname;
    private String lastname;
    private String contact;
    private Set<Role> roles = new HashSet<>();

}
