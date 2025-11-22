package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.task.TaskRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.task.TaskResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.Task;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {UserLightweightMapper.class, TaskCommentMapper.class}
)
public interface TaskMapper {

    @Mapping(target = "assignedBy", source = "assignedBy")
    @Mapping(target = "claimedBy", source = "claimedBy")
    @Mapping(target = "completedBy", source = "completedBy")
    @Mapping(target = "subscriptionId", expression = "java(mapSubscriptionId(task))")
    @Mapping(target = "subscriptionServiceName", expression = "java(mapSubscriptionServiceName(task))")
    @Mapping(target = "subscriptionUsername", expression = "java(mapSubscriptionUsername(task))")
    @Mapping(target = "isOverdue", expression = "java(task.isOverdue())")
    @Mapping(target = "isClaimed", expression = "java(task.isClaimed())")
    @Mapping(target = "commentCount", expression = "java(getCommentCount(task))")
    @Mapping(target = "comments", source = "comments")
    TaskResponseDto toDto(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "assignedBy", ignore = true)
    @Mapping(target = "claimedBy", ignore = true)
    @Mapping(target = "completedBy", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Task toEntity(TaskRequestDto taskRequestDto);

    default Long mapSubscriptionId(Task task) {
        if (task == null || task.getSubscription() == null) {
            return null;
        }
        return task.getSubscription().getId();
    }

    default String mapSubscriptionServiceName(Task task) {
        if (task == null || task.getSubscription() == null || task.getSubscription().getService() == null) {
            return null;
        }
        return task.getSubscription().getService().getName();
    }

    default String mapSubscriptionUsername(Task task) {
        if (task == null || task.getSubscription() == null || task.getSubscription().getUser() == null) {
            return null;
        }
        return task.getSubscription().getUser().getUsername();
    }

    default Integer getCommentCount(Task task) {
        if (task == null || task.getComments() == null) {
            return 0;
        }
        return (int) task.getComments().stream()
                .filter(comment -> !comment.getDeleted())
                .count();
    }

    List<TaskResponseDto> toDtoList(List<Task> tasks);
}
