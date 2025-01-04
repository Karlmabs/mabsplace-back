package com.mabsplace.mabsplaceback.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profile_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id")
    private UserProfile profile;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
}
