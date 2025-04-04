package com.mabsplace.mabsplaceback.domain.repositories;


import com.mabsplace.mabsplaceback.domain.entities.Profile;
import com.mabsplace.mabsplaceback.domain.enums.ProfileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    List<Profile> findByServiceAccountId(Long serviceAccountId);
    
    List<Profile> findByServiceAccountIdAndStatus(Long serviceAccountId, ProfileStatus status);
    
   // find available profiles by serviceid
    @Query("SELECT p FROM Profile p " +
            "JOIN ServiceAccount sa ON sa.id = p.serviceAccount.id " +
            "WHERE sa.myService.id = :serviceId AND p.status = :status")
    List<Profile> findAvailableProfilesByServiceId(@Param("serviceId") Long serviceId, @Param("status") ProfileStatus status);

    // find available profiles by serviceid and user id
//    @Query("SELECT p FROM Profile p " +
//            "JOIN ServiceAccount sa ON sa.id = p.serviceAccount.id " +
//            "JOIN Subscription s ON s.service.id = sa.myService.id " +
//            "WHERE s.user.id = :userId AND sa.myService.id = :serviceId AND p.status = :status")
//    List<Profile> findAvailableProfilesByUserIdAndServiceId(@Param("userId") Long userId, @Param("serviceId") Long serviceId, @Param("status") ProfileStatus status);
}
