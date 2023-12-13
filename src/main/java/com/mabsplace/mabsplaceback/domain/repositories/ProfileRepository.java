package com.mabsplace.mabsplaceback.domain.repositories;


import com.mabsplace.mabsplaceback.domain.entities.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
