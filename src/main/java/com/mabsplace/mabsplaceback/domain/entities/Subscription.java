package com.mabsplace.mabsplaceback.domain.entities;

import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "subscriptions")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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

  @OneToOne
  @JoinColumn(name = "profile_id", referencedColumnName = "id")
  private Profile profile;

  private Date startDate;

  private Date endDate;

  @Enumerated(EnumType.STRING)
  private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

}
