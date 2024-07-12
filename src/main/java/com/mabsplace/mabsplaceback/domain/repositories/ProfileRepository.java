package com.mabsplace.mabsplaceback.domain.repositories;


import com.mabsplace.mabsplaceback.domain.entities.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    List<Profile> findByServiceAccountId(Long serviceAccountId);
}
