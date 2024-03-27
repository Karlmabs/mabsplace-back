package com.mabsplace.mabsplaceback.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "currencies")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Currency {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true)
  private String name;
  @Column(nullable = false, unique = true)
  private Double exchangeRate;
  private String symbol;

}
