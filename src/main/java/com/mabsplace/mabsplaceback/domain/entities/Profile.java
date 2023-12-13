package com.mabsplace.mabsplaceback.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profiles")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Profile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "account_id", referencedColumnName = "id")
  private ServiceAccount serviceAccount;

  @OneToOne(mappedBy = "profile")
  private Subscription subscription;

  private String profileName;

}
