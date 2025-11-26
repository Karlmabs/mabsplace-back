package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.Task;
import com.mabsplace.mabsplaceback.domain.enums.TaskPriority;
import com.mabsplace.mabsplaceback.domain.enums.TaskStatus;
import com.mabsplace.mabsplaceback.domain.enums.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Find all non-deleted tasks
    List<Task> findByDeletedFalseOrderByCreatedAtDesc();

    // Find tasks by status
    List<Task> findByStatusAndDeletedFalseOrderByCreatedAtDesc(TaskStatus status);

    // Find tasks by type
    List<Task> findByTypeAndDeletedFalseOrderByCreatedAtDesc(TaskType type);

    // Find tasks by priority
    List<Task> findByPriorityAndDeletedFalseOrderByDueDateAsc(TaskPriority priority);

    // Find tasks claimed by a specific user
    List<Task> findByClaimedByIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    // Find unclaimed tasks
    List<Task> findByClaimedByIsNullAndDeletedFalseOrderByDueDateAsc();

    // Find overdue tasks (due date passed and not completed)
    List<Task> findByDueDateBeforeAndStatusNotAndDeletedFalseOrderByDueDateAsc(Date date, TaskStatus status);

    // Find tasks by subscription
    List<Task> findBySubscriptionIdAndDeletedFalseOrderByCreatedAtDesc(Long subscriptionId);

    // Custom query to find tasks with multiple filters
    @Query("SELECT t FROM Task t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:claimedById IS NULL OR t.claimedBy.id = :claimedById) AND " +
           "t.deleted = false " +
           "ORDER BY t.createdAt DESC")
    List<Task> findTasksWithFilters(
        @Param("status") TaskStatus status,
        @Param("type") TaskType type,
        @Param("priority") TaskPriority priority,
        @Param("claimedById") Long claimedById
    );

    // Count tasks by status
    long countByStatusAndDeletedFalse(TaskStatus status);

    // Check if task exists for a subscription with specific status
    boolean existsBySubscriptionIdAndStatusAndDeletedFalse(Long subscriptionId, TaskStatus status);

    // Check if task exists by type and metadata containing string with specific statuses
    boolean existsByTypeAndMetadataContainingAndStatusIn(TaskType type, String metadataFragment, List<TaskStatus> statuses);
}
