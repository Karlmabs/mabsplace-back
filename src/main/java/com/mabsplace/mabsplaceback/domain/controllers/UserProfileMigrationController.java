package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.userProfile.MigrationStatus;
import com.mabsplace.mabsplaceback.domain.services.UserProfileMigrationService;
import com.mabsplace.mabsplaceback.security.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(UserProfileMigrationController.class);

    @PostMapping("/migrate-users")
    public ResponseEntity<?> migrateUsers() {
        logger.info("User migration process initiated.");
        migrationService.performManualMigration();
        logger.info("User migration process completed successfully.");
        return ResponseEntity.ok(new MessageResponse("User migration completed successfully."));
    }

    @GetMapping("/status")
    public ResponseEntity<MigrationStatus> getMigrationStatus() {
        logger.info("Fetching migration status.");
        MigrationStatus status = migrationService.checkMigrationStatus();
        logger.info("Migration status fetched: {}", status);
        return ResponseEntity.ok(status);
    }
}
