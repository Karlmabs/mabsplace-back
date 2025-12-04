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

    // Find profiles that are truly available (not referenced by any subscription)
    @Query("SELECT p FROM Profile p " +
            "JOIN ServiceAccount sa ON sa.id = p.serviceAccount.id " +
            "WHERE sa.myService.id = :serviceId AND p.status = :status " +
            "AND p.id NOT IN (SELECT DISTINCT s.profile.id FROM Subscription s WHERE s.profile.id IS NOT NULL)")
    List<Profile> findTrulyAvailableProfilesByServiceId(@Param("serviceId") Long serviceId, @Param("status") ProfileStatus status);

    // Profile utilization queries for dashboard analytics
    @Query("SELECT COUNT(p) FROM Profile p WHERE p.status = :status")
    Long countByStatus(@Param("status") ProfileStatus status);

    @Query("SELECT COUNT(p) FROM Profile p")
    Long countTotalProfiles();

    @Query("SELECT COUNT(DISTINCT p) FROM Profile p " +
            "JOIN Subscription s ON s.profile.id = p.id " +
            "WHERE s.status = 'ACTIVE'")
    Long countActivelyUsedProfiles();

    // Get utilization rate by service
    @Query("SELECT s.name as serviceName, " +
            "COUNT(DISTINCT p.id) as totalProfiles, " +
            "COUNT(DISTINCT CASE WHEN p.status = 'ACTIVE' THEN p.id END) as activeProfiles, " +
            "COUNT(DISTINCT CASE WHEN sub.status = 'ACTIVE' THEN p.id END) as usedProfiles " +
            "FROM MyService s " +
            "JOIN ServiceAccount sa ON sa.myService.id = s.id " +
            "JOIN Profile p ON p.serviceAccount.id = sa.id " +
            "LEFT JOIN Subscription sub ON sub.profile.id = p.id AND sub.status = 'ACTIVE' " +
            "GROUP BY s.id, s.name")
    List<Object[]> getUtilizationRateByService();
}
