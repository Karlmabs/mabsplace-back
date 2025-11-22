package com.mabsplace.mabsplaceback.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "task_comments")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TaskComment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "task_id", referencedColumnName = "id", nullable = false)
  private Task task;

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
  private User user;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Date createdAt;

  @Column(nullable = false)
  @Builder.Default
  private Boolean deleted = false;

  @PrePersist
  protected void onCreate() {
    if (deleted == null) {
      deleted = false;
    }
  }
}
