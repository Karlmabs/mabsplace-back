package com.mabsplace.mabsplaceback.domain.dtos.userProfile;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignProfileRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Profile ID is required")
    private Long profileId;
}
