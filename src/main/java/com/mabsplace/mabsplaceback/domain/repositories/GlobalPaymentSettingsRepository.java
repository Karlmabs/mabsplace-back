package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.GlobalPaymentSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GlobalPaymentSettingsRepository extends JpaRepository<GlobalPaymentSettings, Long> {
    // There should only be one settings record, so we can get the first one
    Optional<GlobalPaymentSettings> findFirstByOrderByIdAsc();
}
