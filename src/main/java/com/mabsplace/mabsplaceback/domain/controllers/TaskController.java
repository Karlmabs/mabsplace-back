package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.task.*;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.domain.services.TaskService;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;
    private final UserRepository userRepository;

    public TaskController(TaskService taskService, UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    // Get all tasks with optional filters
    @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'MANAGE_TASKS')")
    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAllTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long claimedById,
            @RequestParam(required = false) String search
    ) {
        logger.info("Fetching all tasks with filters - status: {}, type: {}, priority: {}, search: {}",
                status, type, priority, search);

        TaskFilterDto filterDto = TaskFilterDto.builder()
                .status(status != null ? com.mabsplace.mabsplaceback.domain.enums.TaskStatus.valueOf(status) : null)
                .type(type != null ? com.mabsplace.mabsplaceback.domain.enums.TaskType.valueOf(type) : null)
                .priority(priority != null ? com.mabsplace.mabsplaceback.domain.enums.TaskPriority.valueOf(priority) : null)
                .claimedById(claimedById)
                .search(search)
                .build();

        List<TaskResponseDto> tasks = taskService.getAllTasks(filterDto);
        logger.info("Fetched {} tasks", tasks.size());

        return ResponseEntity.ok(tasks);
    }

    // Get task by ID
    @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'MANAGE_TASKS')")
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id) {
        logger.info("Fetching task with ID: {}", id);
        TaskResponseDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    // Create manual task
    @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'MANAGE_TASKS')")
    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(
            @RequestBody TaskRequestDto taskRequestDto,
            Authentication authentication
    ) {
        User user = getCurrentUser(authentication);
        logger.info("User {} creating new task: {}", user.getUsername(), taskRequestDto.getTitle());

        TaskResponseDto createdTask = taskService.createTask(taskRequestDto, user.getId());
        logger.info("Task created successfully with ID: {}", createdTask.getId());

        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    // Update task
    @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'MANAGE_TASKS')")
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long id,
            @RequestBody TaskRequestDto taskRequestDto,
            Authentication authentication
    ) {
        User user = getCurrentUser(authentication);
        logger.info("User {} updating task ID: {}", user.getUsername(), id);

        TaskResponseDto updatedTask = taskService.updateTask(id, taskRequestDto, user.getId());
        logger.info("Task updated successfully: {}", id);

        return ResponseEntity.ok(updatedTask);
    }

    // Update task status
    @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'MANAGE_TASKS')")
    @PutMapping("/{id}/status")
    public ResponseEntity<TaskResponseDto> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody TaskUpdateStatusDto statusDto,
            Authentication authentication
    ) {
        User user = getCurrentUser(authentication);
        logger.info("User {} updating task status for ID: {} to {}", user.getUsername(), id, statusDto.getStatus());

        TaskResponseDto updatedTask = taskService.updateTaskStatus(id, statusDto, user.getId());
        logger.info("Task status updated successfully: {}", id);

        return ResponseEntity.ok(updatedTask);
    }

    // Claim task
    @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'MANAGE_TASKS')")
    @PutMapping("/{id}/claim")
    public ResponseEntity<TaskResponseDto> claimTask(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User user = getCurrentUser(authentication);
        logger.info("User {} claiming task ID: {}", user.getUsername(), id);

        TaskResponseDto updatedTask = taskService.claimTask(id, user.getId());
        logger.info("Task claimed successfully by user: {}", user.getUsername());

        return ResponseEntity.ok(updatedTask);
    }

    // Unclaim task
    @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'MANAGE_TASKS')")
    @PutMapping("/{id}/unclaim")
    public ResponseEntity<TaskResponseDto> unclaimTask(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User user = getCurrentUser(authentication);
        logger.info("User {} unclaiming task ID: {}", user.getUsername(), id);

        TaskResponseDto updatedTask = taskService.unclaimTask(id, user.getId());
        logger.info("Task unclaimed successfully: {}", id);

        return ResponseEntity.ok(updatedTask);
    }

    // Delete task
    @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'MANAGE_TASKS')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        logger.info("Deleting task ID: {}", id);
        taskService.deleteTask(id);
        logger.info("Task deleted successfully: {}", id);

        return ResponseEntity.noContent().build();
    }

    // Add comment to task
    @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'MANAGE_TASKS')")
    @PostMapping("/{taskId}/comments")
    public ResponseEntity<TaskCommentResponseDto> addComment(
            @PathVariable Long taskId,
            @RequestBody TaskCommentRequestDto commentRequestDto,
            Authentication authentication
    ) {
        User user = getCurrentUser(authentication);
        logger.info("User {} adding comment to task ID: {}", user.getUsername(), taskId);

        TaskCommentResponseDto comment = taskService.addComment(taskId, commentRequestDto, user.getId());
        logger.info("Comment added successfully to task: {}", taskId);

        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    // Get comments for a task
    @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'MANAGE_TASKS')")
    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<TaskCommentResponseDto>> getTaskComments(@PathVariable Long taskId) {
        logger.info("Fetching comments for task ID: {}", taskId);
        List<TaskCommentResponseDto> comments = taskService.getTaskComments(taskId);
        logger.info("Fetched {} comments for task: {}", comments.size(), taskId);

        return ResponseEntity.ok(comments);
    }

    // Get task statistics
    @PreAuthorize("@securityExpressionUtil.hasAnyRole(authentication, 'MANAGE_TASKS')")
    @GetMapping("/stats")
    public ResponseEntity<TaskStatsDto> getTaskStats() {
        logger.info("Fetching task statistics");
        TaskStatsDto stats = taskService.getTaskStats();
        logger.info("Task statistics: TODO={}, IN_PROGRESS={}, DONE={}, OVERDUE={}",
                stats.getTodoCount(), stats.getInProgressCount(), stats.getDoneCount(), stats.getOverdueCount());

        return ResponseEntity.ok(stats);
    }
}
