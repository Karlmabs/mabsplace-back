package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.task.*;
import com.mabsplace.mabsplaceback.domain.entities.Subscription;
import com.mabsplace.mabsplaceback.domain.entities.Task;
import com.mabsplace.mabsplaceback.domain.entities.TaskComment;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.enums.TaskPriority;
import com.mabsplace.mabsplaceback.domain.enums.TaskStatus;
import com.mabsplace.mabsplaceback.domain.enums.TaskType;
import com.mabsplace.mabsplaceback.domain.mappers.TaskCommentMapper;
import com.mabsplace.mabsplaceback.domain.mappers.TaskMapper;
import com.mabsplace.mabsplaceback.domain.repositories.SubscriptionRepository;
import com.mabsplace.mabsplaceback.domain.repositories.TaskCommentRepository;
import com.mabsplace.mabsplaceback.domain.repositories.TaskRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskMapper taskMapper;
    private final TaskCommentMapper taskCommentMapper;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public TaskService(TaskRepository taskRepository, TaskCommentRepository taskCommentRepository,
                       TaskMapper taskMapper, TaskCommentMapper taskCommentMapper,
                       UserRepository userRepository, SubscriptionRepository subscriptionRepository) {
        this.taskRepository = taskRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.taskMapper = taskMapper;
        this.taskCommentMapper = taskCommentMapper;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    // Get all tasks with optional filters
    public List<TaskResponseDto> getAllTasks(TaskFilterDto filterDto) {
        logger.info("Fetching tasks with filters: {}", filterDto);

        List<Task> tasks;
        if (filterDto == null || (filterDto.getStatus() == null && filterDto.getType() == null
                && filterDto.getPriority() == null && filterDto.getClaimedById() == null)) {
            tasks = taskRepository.findByDeletedFalseOrderByCreatedAtDesc();
        } else {
            tasks = taskRepository.findTasksWithFilters(
                filterDto.getStatus(),
                filterDto.getType(),
                filterDto.getPriority(),
                filterDto.getClaimedById()
            );
        }

        // Apply search filter if provided
        if (filterDto != null && filterDto.getSearch() != null && !filterDto.getSearch().isEmpty()) {
            String searchLower = filterDto.getSearch().toLowerCase();
            tasks = tasks.stream()
                .filter(task -> task.getTitle().toLowerCase().contains(searchLower)
                        || (task.getDescription() != null && task.getDescription().toLowerCase().contains(searchLower)))
                .collect(Collectors.toList());
        }

        return taskMapper.toDtoList(tasks);
    }

    // Get task by ID
    public TaskResponseDto getTaskById(Long id) {
        logger.info("Fetching task with ID: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        if (task.getDeleted()) {
            throw new ResourceNotFoundException("Task", "id", id);
        }

        return taskMapper.toDto(task);
    }

    // Create manual task
    @Transactional
    public TaskResponseDto createTask(TaskRequestDto taskRequestDto, Long userId) {
        logger.info("Creating new task: {}", taskRequestDto.getTitle());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Task task = taskMapper.toEntity(taskRequestDto);
        task.setAssignedBy(user);
        task.setType(taskRequestDto.getType() != null ? taskRequestDto.getType() : TaskType.MANUAL);
        task.setPriority(taskRequestDto.getPriority() != null ? taskRequestDto.getPriority() : TaskPriority.NORMAL);
        task.setStatus(TaskStatus.TODO);

        // Link to subscription if provided
        if (taskRequestDto.getSubscriptionId() != null) {
            Subscription subscription = subscriptionRepository.findById(taskRequestDto.getSubscriptionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", taskRequestDto.getSubscriptionId()));
            task.setSubscription(subscription);
        }

        Task savedTask = taskRepository.save(task);
        logger.info("Task created successfully with ID: {}", savedTask.getId());

        return taskMapper.toDto(savedTask);
    }

    // Create automated subscription reminder task
    @Transactional
    public TaskResponseDto createSubscriptionReminderTask(Subscription subscription, int daysUntilExpiry) {
        logger.info("Creating subscription reminder task for subscription ID: {}", subscription.getId());

        // Check if task already exists for this subscription and is not done
        boolean taskExists = taskRepository.existsBySubscriptionIdAndStatusAndDeletedFalse(
                subscription.getId(), TaskStatus.TODO) ||
                taskRepository.existsBySubscriptionIdAndStatusAndDeletedFalse(
                subscription.getId(), TaskStatus.IN_PROGRESS);

        if (taskExists) {
            logger.info("Task already exists for subscription ID: {}", subscription.getId());
            return null;
        }

        // Determine priority based on days until expiry
        TaskPriority priority = daysUntilExpiry <= 3 ? TaskPriority.URGENT : TaskPriority.HIGH;

        // Create WhatsApp message template
        String customerName = subscription.getUser().getFirstname() != null
                ? subscription.getUser().getFirstname()
                : subscription.getUser().getUsername();
        String serviceName = subscription.getService().getName();
        String phoneNumber = subscription.getUser().getPhonenumber();

        String whatsappMessage = String.format(
            "Bonjour %s,\n\n" +
            "Ceci est un rappel amical que votre abonnement %s expirera dans %d jour(s).\n\n" +
            "Pour continuer à profiter de nos services sans interruption, veuillez renouveler votre abonnement dès que possible.\n\n" +
            "Si vous avez des questions, n'hésitez pas à nous contacter.\n\n" +
            "Cordialement,\n" +
            "L'équipe MabsPlace",
            customerName, serviceName, daysUntilExpiry
        );

        String metadata = String.format(
            "{\"customerName\": \"%s\", \"serviceName\": \"%s\", \"phoneNumber\": \"%s\", \"daysUntilExpiry\": %d, \"expiryDate\": \"%s\"}",
            customerName, serviceName, phoneNumber != null ? phoneNumber : "N/A", daysUntilExpiry, subscription.getEndDate()
        );

        Task task = Task.builder()
                .title(String.format("Remind customer: %s - %s (Expires in %d days)", customerName, serviceName, daysUntilExpiry))
                .description(whatsappMessage)
                .type(TaskType.SUBSCRIPTION_REMINDER)
                .priority(priority)
                .status(TaskStatus.TODO)
                .dueDate(subscription.getEndDate())
                .metadata(metadata)
                .subscription(subscription)
                .build();

        Task savedTask = taskRepository.save(task);
        logger.info("Subscription reminder task created successfully with ID: {}", savedTask.getId());

        return taskMapper.toDto(savedTask);
    }

    // Create automated task for failed subscription renewal
    @Transactional
    public TaskResponseDto createSubscriptionRenewalFailedTask(Subscription subscription) {
        logger.info("Creating subscription renewal failed task for subscription ID: {}", subscription.getId());

        String customerName = subscription.getUser().getFirstname() != null
                ? subscription.getUser().getFirstname()
                : subscription.getUser().getUsername();
        String serviceName = subscription.getService().getName();
        String phoneNumber = subscription.getUser().getPhonenumber();

        // Create French WhatsApp message template
        String whatsappMessage = String.format(
            "Bonjour %s,\n\n" +
            "Nous vous contactons concernant votre abonnement %s.\n\n" +
            "Le renouvellement automatique de votre abonnement a échoué. " +
            "Pour continuer à profiter de nos services sans interruption, veuillez renouveler votre abonnement dès que possible.\n\n" +
            "Si vous avez des questions ou besoin d'assistance, n'hésitez pas à nous contacter.\n\n" +
            "Cordialement,\n" +
            "L'équipe MabsPlace",
            customerName, serviceName
        );

        String metadata = String.format(
            "{\"customerName\": \"%s\", \"serviceName\": \"%s\", \"phoneNumber\": \"%s\"}",
            customerName, serviceName, phoneNumber != null ? phoneNumber : "N/A"
        );

        Task task = Task.builder()
                .title(String.format("Subscription Renewal Failed: %s - %s", customerName, serviceName))
                .description(whatsappMessage)
                .type(TaskType.SUBSCRIPTION_RENEWAL_FAILED)
                .priority(TaskPriority.URGENT)
                .status(TaskStatus.TODO)
                .metadata(metadata)
                .subscription(subscription)
                .build();

        Task savedTask = taskRepository.save(task);
        logger.info("Subscription renewal failed task created successfully with ID: {}", savedTask.getId());

        return taskMapper.toDto(savedTask);
    }

    // Create automated task for post-expiration actions
    @Transactional
    public TaskResponseDto createPostExpirationTask(Subscription subscription) {
        logger.info("Creating post-expiration task for subscription ID: {}", subscription.getId());

        String customerName = subscription.getUser().getFirstname() != null
                ? subscription.getUser().getFirstname()
                : subscription.getUser().getUsername();
        String serviceName = subscription.getService().getName();

        Task task = Task.builder()
                .title(String.format("Post-Expiration Action: %s - %s", customerName, serviceName))
                .description("The subscription has expired. Please change the credentials or remove the user from the family plan.")
                .type(TaskType.POST_EXPIRATION)
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.TODO)
                .subscription(subscription)
                .build();

        Task savedTask = taskRepository.save(task);
        logger.info("Post-expiration task created successfully with ID: {}", savedTask.getId());

        return taskMapper.toDto(savedTask);
    }

    // Create automated task for inactive customer follow-up
    @Transactional
    public TaskResponseDto createInactiveCustomerFollowupTask(User user, int daysInactive,
                                                              Date lastPaymentDate,
                                                              java.math.BigDecimal lastPaymentAmount) {
        logger.info("Creating inactive customer follow-up task for user ID: {}", user.getId());

        String customerName = user.getFirstname() != null
                ? user.getFirstname()
                : user.getUsername();
        String phoneNumber = user.getPhonenumber();

        String whatsappMessage = String.format(
            "Bonjour %s,\n\n" +
            "Nous espérons que vous allez bien !\n\n" +
            "Nous avons remarqué que vous n'avez pas eu d'abonnement actif depuis environ %d jours. " +
            "Nous voulions prendre des nouvelles et savoir si vous souhaitez reprendre un abonnement.\n\n" +
            "Si vous avez des questions ou si nous pouvons vous aider de quelque manière que ce soit, " +
            "n'hésitez pas à nous contacter.\n\n" +
            "Nous serions ravis de vous revoir parmi nos clients !\n\n" +
            "Cordialement,\n" +
            "L'équipe MabsPlace",
            customerName, daysInactive
        );

        // Format payment date and amount for metadata
        String paymentDateStr = lastPaymentDate != null
                ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(lastPaymentDate)
                : "N/A";
        String paymentAmountStr = lastPaymentAmount != null
                ? String.format("%.2f", lastPaymentAmount)
                : "0.00";

        String metadata = String.format(
            "{\"userId\": %d, \"customerName\": \"%s\", \"phoneNumber\": \"%s\", \"email\": \"%s\", " +
            "\"daysInactive\": %d, \"lastPaymentDate\": \"%s\", \"lastPaymentAmount\": %s}",
            user.getId(), customerName,
            phoneNumber != null ? phoneNumber : "N/A",
            user.getEmail(), daysInactive,
            paymentDateStr, paymentAmountStr
        );

        Task task = Task.builder()
                .title(String.format("Follow up: Inactive customer %s (%d days)", customerName, daysInactive))
                .description(whatsappMessage)
                .type(TaskType.INACTIVE_CUSTOMER_FOLLOWUP)
                .priority(TaskPriority.NORMAL)
                .status(TaskStatus.TODO)
                .metadata(metadata)
                .build();

        Task savedTask = taskRepository.save(task);
        logger.info("Inactive customer follow-up task created successfully with ID: {}", savedTask.getId());

        return taskMapper.toDto(savedTask);
    }

    // Update task
    @Transactional
    public TaskResponseDto updateTask(Long id, TaskRequestDto taskRequestDto, Long userId) {
        logger.info("Updating task ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        if (task.getDeleted()) {
            throw new ResourceNotFoundException("Task", "id", id);
        }

        if (taskRequestDto.getTitle() != null) task.setTitle(taskRequestDto.getTitle());
        if (taskRequestDto.getDescription() != null) task.setDescription(taskRequestDto.getDescription());
        if (taskRequestDto.getPriority() != null) task.setPriority(taskRequestDto.getPriority());
        if (taskRequestDto.getDueDate() != null) task.setDueDate(taskRequestDto.getDueDate());
        if (taskRequestDto.getMetadata() != null) task.setMetadata(taskRequestDto.getMetadata());

        Task updatedTask = taskRepository.save(task);
        logger.info("Task updated successfully: {}", id);

        return taskMapper.toDto(updatedTask);
    }

    // Update task status
    @Transactional
    public TaskResponseDto updateTaskStatus(Long id, TaskUpdateStatusDto statusDto, Long userId) {
        logger.info("Updating task status for ID: {} to {}", id, statusDto.getStatus());

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        if (task.getDeleted()) {
            throw new ResourceNotFoundException("Task", "id", id);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        task.setStatus(statusDto.getStatus());

        // If status is DONE, set completedAt and completedBy
        if (statusDto.getStatus() == TaskStatus.DONE) {
            task.setCompletedAt(new Date());
            task.setCompletedBy(user);
        } else {
            task.setCompletedAt(null);
            task.setCompletedBy(null);
        }

        Task updatedTask = taskRepository.save(task);
        logger.info("Task status updated successfully: {}", id);

        return taskMapper.toDto(updatedTask);
    }

    // Claim task
    @Transactional
    public TaskResponseDto claimTask(Long id, Long userId) {
        logger.info("User {} claiming task ID: {}", userId, id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        if (task.getDeleted()) {
            throw new ResourceNotFoundException("Task", "id", id);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        task.setClaimedBy(user);

        // Automatically set status to IN_PROGRESS if it's TODO
        if (task.getStatus() == TaskStatus.TODO) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }

        Task updatedTask = taskRepository.save(task);
        logger.info("Task claimed successfully by user: {}", userId);

        return taskMapper.toDto(updatedTask);
    }

    // Unclaim task
    @Transactional
    public TaskResponseDto unclaimTask(Long id, Long userId) {
        logger.info("User {} unclaiming task ID: {}", userId, id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        if (task.getDeleted()) {
            throw new ResourceNotFoundException("Task", "id", id);
        }

        // Only the user who claimed it can unclaim it
        if (task.getClaimedBy() == null || !task.getClaimedBy().getId().equals(userId)) {
            throw new IllegalStateException("You can only unclaim tasks that you have claimed");
        }

        task.setClaimedBy(null);

        // Set status back to TODO if it's IN_PROGRESS
        if (task.getStatus() == TaskStatus.IN_PROGRESS) {
            task.setStatus(TaskStatus.TODO);
        }

        Task updatedTask = taskRepository.save(task);
        logger.info("Task unclaimed successfully: {}", id);

        return taskMapper.toDto(updatedTask);
    }

    // Delete task (soft delete)
    @Transactional
    public void deleteTask(Long id) {
        logger.info("Deleting task ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        task.setDeleted(true);
        taskRepository.save(task);

        logger.info("Task deleted successfully: {}", id);
    }

    // Add comment to task
    @Transactional
    public TaskCommentResponseDto addComment(Long taskId, TaskCommentRequestDto commentRequestDto, Long userId) {
        logger.info("Adding comment to task ID: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (task.getDeleted()) {
            throw new ResourceNotFoundException("Task", "id", taskId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        TaskComment comment = taskCommentMapper.toEntity(commentRequestDto);
        comment.setTask(task);
        comment.setUser(user);

        TaskComment savedComment = taskCommentRepository.save(comment);
        logger.info("Comment added successfully to task: {}", taskId);

        return taskCommentMapper.toDto(savedComment);
    }

    // Get comments for a task
    public List<TaskCommentResponseDto> getTaskComments(Long taskId) {
        logger.info("Fetching comments for task ID: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (task.getDeleted()) {
            throw new ResourceNotFoundException("Task", "id", taskId);
        }

        List<TaskComment> comments = taskCommentRepository.findByTaskIdAndDeletedFalseOrderByCreatedAtAsc(taskId);
        return taskCommentMapper.toDtoList(comments);
    }

    // Get task statistics
    public TaskStatsDto getTaskStats() {
        logger.info("Fetching task statistics");

        long todoCount = taskRepository.countByStatusAndDeletedFalse(TaskStatus.TODO);
        long inProgressCount = taskRepository.countByStatusAndDeletedFalse(TaskStatus.IN_PROGRESS);
        long doneCount = taskRepository.countByStatusAndDeletedFalse(TaskStatus.DONE);

        List<Task> overdueTasks = taskRepository.findByDueDateBeforeAndStatusNotAndDeletedFalseOrderByDueDateAsc(
                new Date(), TaskStatus.DONE);
        long overdueCount = overdueTasks.size();

        return TaskStatsDto.builder()
                .todoCount(todoCount)
                .inProgressCount(inProgressCount)
                .doneCount(doneCount)
                .overdueCount(overdueCount)
                .build();
    }
}
