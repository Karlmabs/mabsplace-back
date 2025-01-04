package com.mabsplace.mabsplaceback.domain.dtos.userProfile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserProfileRequest {
    @NotBlank(message = "Profile name is required")
    private String name;

    private String description;

    @NotEmpty(message = "At least one role is required")
    private Set<String> roleNames;
}
