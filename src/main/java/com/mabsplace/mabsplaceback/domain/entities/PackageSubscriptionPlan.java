package com.mabsplace.mabsplaceback.domain.entities;

import com.mabsplace.mabsplaceback.domain.enums.Period;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Subscription plan for a service package
 * This is a special type of subscription plan that applies to a package of services
 * instead of a single service
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "package_subscription_plans")
public class PackageSubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Period period;
    
    @ManyToOne
    @JoinColumn(name = "currency_id", referencedColumnName = "id")
    private Currency currency;
    
    @Column(nullable = false)
    private String description;
    
    /**
     * The service package this plan applies to
     */
    @ManyToOne
    @JoinColumn(name = "package_id", referencedColumnName = "id")
    private ServicePackage servicePackage;
    
    /**
     * Flag to determine if this plan is active
     */
    private boolean active = true;
}
