package com.mabsplace.mabsplaceback.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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

  // Getters and Setters
}
