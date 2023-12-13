package com.mabsplace.mabsplaceback.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
  private String accountDetails;

  @ManyToOne
  @JoinColumn(name = "service_id", referencedColumnName = "id")
  private MyService myService;

  @OneToMany(mappedBy = "serviceAccount")
  private List<Profile> profiles;
}
