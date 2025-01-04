package com.mabsplace.mabsplaceback.domain.dtos.userProfile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MigrationStatus {
    private long totalUsers;
    private long usersWithProfiles;
    private long usersWithoutProfiles;
    private boolean migrationComplete;
}
