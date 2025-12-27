package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.entities.UserProfile;
import com.mabsplace.mabsplaceback.domain.enums.AuthenticationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  Boolean existsByUsername(String username);

  @Query("SELECT u FROM User u WHERE LOWER(TRIM(u.username)) = LOWER(TRIM(:username))")
  Optional<User> findByUsernameIgnoreCase(@Param("username") String username);

  @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(TRIM(u.username)) = LOWER(TRIM(:username))")
  Boolean existsByUsernameIgnoreCase(@Param("username") String username);

  Boolean existsByEmail(String email);
  
  Boolean existsByPhonenumber(String phonenumber);
  
  Optional<User> findByReferralCode(String referralCode);
  
  Boolean existsByReferralCode(String referralCode);

  @Modifying
  @Query("UPDATE User u SET u.authType = ?2 WHERE u.username = ?1")
  public void updateAuthenticationType(String username, AuthenticationType authType);

  Optional<User> findByEmail(String email);

  @Query("SELECT u.subscriptions FROM User u WHERE u.id = ?1")
  List<Subscription> getSubscriptionsByUserId(Long id);

    List<User> findByUserProfile(UserProfile profile);

  List<User> findByUserProfileIsNull();

  long countByUserProfileIsNotNull();
}
