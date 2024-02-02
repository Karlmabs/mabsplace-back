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
  private String description;

  @OneToMany(mappedBy = "myService", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ServiceAccount> serviceAccounts;

  @OneToMany(mappedBy = "myService", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<SubscriptionPlan> subscriptionPlans = new LinkedHashSet<>();

  public List<ServiceAccount> getAvailableAccounts() {
    List<ServiceAccount> availableAccounts = new ArrayList<>();

    for (ServiceAccount account : serviceAccounts) {
      if (!account.getAvailableProfiles().isEmpty()) {
        availableAccounts.add(account);
      }
    }
    return availableAccounts;
  }

}
