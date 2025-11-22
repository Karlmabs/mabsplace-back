package com.mabsplace.mabsplaceback.domain.mappers;

import com.mabsplace.mabsplaceback.domain.dtos.task.TaskCommentRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.task.TaskCommentResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.TaskComment;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {UserLightweightMapper.class}
)
public interface TaskCommentMapper {

    @Mapping(target = "taskId", expression = "java(mapTaskId(taskComment))")
    @Mapping(target = "user", source = "user")
    TaskCommentResponseDto toDto(TaskComment taskComment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "task", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    TaskComment toEntity(TaskCommentRequestDto taskCommentRequestDto);

    default Long mapTaskId(TaskComment taskComment) {
        if (taskComment == null || taskComment.getTask() == null) {
            return null;
        }
        return taskComment.getTask().getId();
    }

    List<TaskCommentResponseDto> toDtoList(List<TaskComment> taskComments);
}
