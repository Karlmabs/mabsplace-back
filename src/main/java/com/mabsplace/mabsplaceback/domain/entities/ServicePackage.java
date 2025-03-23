package com.mabsplace.mabsplaceback.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Service Package entity that groups multiple services together
 * with a discounted price compared to individual subscriptions
 */
@Entity
@Table(name = "service_packages")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ServicePackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String description;
    
    /**
     * The services included in this package
     */
    @ManyToMany
    @JoinTable(
        name = "package_services",
        joinColumns = @JoinColumn(name = "package_id"),
        inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private Set<MyService> services = new HashSet<>();
    
    /**
     * The image to display for this package
     */
    private String image;
    
    /**
     * Flag to determine if this package is active
     */
    private boolean active = true;
}
