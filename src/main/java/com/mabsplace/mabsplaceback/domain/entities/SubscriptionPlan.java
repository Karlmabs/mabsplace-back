package com.mabsplace.mabsplaceback.domain.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name; // e.g., "Monthly", "Yearly"

  @Column(nullable = false)
  private BigDecimal price;

  @Column(nullable = false)
  private String description;

  @ManyToOne
  @JoinColumn(name = "service_id", referencedColumnName = "id")
  private MyService myService;

  // Getters and Setters
}
