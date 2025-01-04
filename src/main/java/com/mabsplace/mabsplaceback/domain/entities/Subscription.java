package com.mabsplace.mabsplaceback.domain.entities;

import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "subscriptions", uniqueConstraints = {
@UniqueConstraint(columnNames = {"user_id", "service_id", "is_trial"})
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Subscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;

  @ManyToOne
  @JoinColumn(name = "plan_id", referencedColumnName = "id")
  private SubscriptionPlan subscriptionPlan;

  @ManyToOne
  @JoinColumn(name = "service_id", referencedColumnName = "id")
  private MyService service;

  @OneToOne
  @JoinColumn(name = "profile_id", referencedColumnName = "id")
  private Profile profile;

  private Date startDate;

  private Date endDate;

  @Enumerated(EnumType.STRING)
  private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

  private boolean autoRenew = true;

  private Integer renewalAttempts = 0;

  private Date lastRenewalAttempt;

  @ManyToOne
  @JoinColumn(name = "next_subscription_plan_id", referencedColumnName = "id")
  private SubscriptionPlan nextSubscriptionPlan;

  // New field for trial tracking
  @Column(name = "is_trial")
  private Boolean isTrial = false;

  @PrePersist
  protected void onCreate() {
    if (renewalAttempts == null) {
      renewalAttempts = 0;
    }
  }

  // Helper methods
  public boolean isExpired() {
    return endDate != null && endDate.before(new Date());
  }

  public boolean isActive() {
    return status == SubscriptionStatus.ACTIVE && !isExpired();
  }

}
