package com.mabsplace.mabsplaceback.domain.entities;

import com.mabsplace.mabsplaceback.domain.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "subscriptions")
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

}
