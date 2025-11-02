package com.mabsplace.mabsplaceback.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "services")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MyService {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private String logo;
  private String image;
  private String description;

  // Credential visibility configuration for end users
  @Column(name = "show_account_credentials")
  private Boolean showAccountCredentials = true;

  @Column(name = "show_profile_name")
  private Boolean showProfileName = true;

  @Column(name = "show_profile_pin")
  private Boolean showProfilePin = true;

  // Service visibility - controls if service is shown to users
  @Column(name = "is_active")
  private Boolean isActive = true;

  @OneToMany(mappedBy = "myService", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ServiceAccount> serviceAccounts;

  @OneToMany(mappedBy = "myService", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<SubscriptionPlan> subscriptionPlans = new LinkedHashSet<>();

}
