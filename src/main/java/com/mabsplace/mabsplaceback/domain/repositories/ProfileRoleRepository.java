package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.ProfileRole;
import com.mabsplace.mabsplaceback.domain.entities.Role;
import com.mabsplace.mabsplaceback.domain.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileRoleRepository extends JpaRepository<ProfileRole, Long> {
    List<ProfileRole> findByProfile(UserProfile profile);
    void deleteByProfileAndRole(UserProfile profile, Role role);
}
