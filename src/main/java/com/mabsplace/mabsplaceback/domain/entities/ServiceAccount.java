package com.mabsplace.mabsplaceback.domain.entities;

import com.mabsplace.mabsplaceback.domain.enums.ProfileStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_accounts")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String login;

  private String password;

  private String accountDetails;

  @ManyToOne
  @JoinColumn(name = "service_id", referencedColumnName = "id")
  private MyService myService;

  @OneToMany(mappedBy = "serviceAccount", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Profile> profiles;

  // return the number of available profiles, not assigned to a user
  public List<Profile> getAvailableProfiles() {
    List<Profile> availableProfiles = new ArrayList<>();

    for (Profile profile : profiles) {
      if (profile.getStatus() == ProfileStatus.INACTIVE) {
        availableProfiles.add(profile);
      }
    }
    return availableProfiles;
  }
}
