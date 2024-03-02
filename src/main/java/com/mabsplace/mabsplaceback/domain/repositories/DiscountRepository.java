package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.Discount;
import com.mabsplace.mabsplaceback.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    //find discount by user
    Optional<Discount> findByUser(User user);

    @Query("SELECT d FROM Discount d WHERE d.user.id = ?1")
    Optional<Discount> findByUserId (Long userId);
}
