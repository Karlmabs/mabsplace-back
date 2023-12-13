package com.mabsplace.mabsplaceback.domain.entities;

import jakarta.persistence.*;
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
  @Column(nullable = false)
  private String password;
  private String contact;

  private String image;

  @ManyToMany
  @JoinTable(joinColumns = @JoinColumn(name = "user_id"))
  private Set<Role> roles = new HashSet<>();

  @OneToOne(mappedBy = "user")
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

}
