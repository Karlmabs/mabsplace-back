package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.userProfile.MigrationStatus;
import com.mabsplace.mabsplaceback.domain.services.UserProfileMigrationService;
import com.mabsplace.mabsplaceback.security.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/migration")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserProfileMigrationController {
    private final UserProfileMigrationService migrationService;

    @PostMapping("/migrate-users")
    public ResponseEntity<?> migrateUsers() {
        migrationService.performManualMigration();
        return ResponseEntity.ok(new MessageResponse("Migration completed successfully"));
    }

    @GetMapping("/status")
    public ResponseEntity<MigrationStatus> getMigrationStatus() {
        return ResponseEntity.ok(migrationService.checkMigrationStatus());
    }
}
