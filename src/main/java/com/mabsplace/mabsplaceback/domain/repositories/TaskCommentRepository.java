package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

    // Find all non-deleted comments for a task
    List<TaskComment> findByTaskIdAndDeletedFalseOrderByCreatedAtAsc(Long taskId);

    // Count comments for a task
    long countByTaskIdAndDeletedFalse(Long taskId);
}
