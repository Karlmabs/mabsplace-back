package com.mabsplace.mabsplaceback.domain.dtos.userProfile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String name;
    private String description;
    private Set<String> roleNames;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}