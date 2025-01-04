package com.mabsplace.mabsplaceback.domain.dtos.userProfile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequest {
    private String description;
    private Set<String> roleNames;
}
