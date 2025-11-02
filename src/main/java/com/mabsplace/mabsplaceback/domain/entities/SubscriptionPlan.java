package com.mabsplace.mabsplaceback.domain.entities;

import com.mabsplace.mabsplaceback.domain.enums.Period;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

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

  // Plan visibility - controls if plan is shown to users
  @Column(name = "is_active")
  private Boolean isActive = true;

  @ManyToOne
  @JoinColumn(name = "service_id", referencedColumnName = "id")
  private MyService myService;

  // Getters and Setters
}
