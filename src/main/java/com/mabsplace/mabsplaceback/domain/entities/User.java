package com.mabsplace.mabsplaceback.domain.entities;

import com.mabsplace.mabsplaceback.domain.enums.AuthProvider;
import com.mabsplace.mabsplaceback.domain.enums.AuthenticationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false, unique = true)
  private String email;
  @Column(nullable = false, unique = true)
  private String phonenumber;
  private String firstname;
  @Column(nullable = false)
  private String lastname;
  private String name;
  @Column(nullable = false)
  private String password;
  private String contact;

  @Column(nullable = false)
  private Boolean emailVerified = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "auth_type")
  private AuthenticationType authType;

  @NotNull
  @Enumerated(EnumType.STRING)
  private AuthProvider provider = AuthProvider.local;

  private String providerId;

  private String image;

  @ManyToMany
  @JoinTable(joinColumns = @JoinColumn(name = "user_id"))
  private Set<Role> roles = new HashSet<>();

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
  private Wallet wallet;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Subscription> subscriptions;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Payment> payments;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Post> posts;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> comments;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Like> likes;

  @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Message> sentMessages;

  @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Message> receivedMessages;

  @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
  private PromoCode promoCode;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Discount> discounts;

}
