package com.mabsplace.mabsplaceback.domain.entities;

import com.mabsplace.mabsplaceback.domain.enums.TaskPriority;
import com.mabsplace.mabsplaceback.domain.enums.TaskStatus;
import com.mabsplace.mabsplaceback.domain.enums.TaskType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "tasks")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Task {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private TaskType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TaskPriority priority = TaskPriority.NORMAL;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TaskStatus status = TaskStatus.TODO;

  private Date dueDate;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Date createdAt;

  private Date completedAt;

  // JSON metadata for storing related information (subscription ID, customer details, etc.)
  @Column(columnDefinition = "JSON")
  private String metadata;

  // Admin who created the task
  @ManyToOne
  @JoinColumn(name = "assigned_by_id", referencedColumnName = "id")
  private User assignedBy;

  // Admin currently working on the task
  @ManyToOne
  @JoinColumn(name = "claimed_by_id", referencedColumnName = "id")
  private User claimedBy;

  // Admin who completed the task
  @ManyToOne
  @JoinColumn(name = "completed_by_id", referencedColumnName = "id")
  private User completedBy;

  // Optional: Link to related subscription for subscription-related tasks
  @ManyToOne
  @JoinColumn(name = "subscription_id", referencedColumnName = "id")
  private Subscription subscription;

  // Comments on this task
  @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<TaskComment> comments = new ArrayList<>();

  // Soft delete flag
  @Column(nullable = false)
  @Builder.Default
  private Boolean deleted = false;

  @PrePersist
  protected void onCreate() {
    if (status == null) {
      status = TaskStatus.TODO;
    }
    if (priority == null) {
      priority = TaskPriority.NORMAL;
    }
    if (deleted == null) {
      deleted = false;
    }
  }

  // Helper methods
  public boolean isOverdue() {
    return dueDate != null && dueDate.before(new Date()) && status != TaskStatus.DONE;
  }

  public boolean isClaimed() {
    return claimedBy != null;
  }

  public boolean isCompleted() {
    return status == TaskStatus.DONE;
  }
}
