package com.mabsplace.mabsplaceback.domain.dtos.task;

import com.mabsplace.mabsplaceback.domain.dtos.user.UserLightweightResponseDto;
import com.mabsplace.mabsplaceback.domain.enums.TaskPriority;
import com.mabsplace.mabsplaceback.domain.enums.TaskStatus;
import com.mabsplace.mabsplaceback.domain.enums.TaskType;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TaskResponseDto implements Serializable {

    private Long id;

    private String title;

    private String description;

    private TaskType type;

    private TaskPriority priority;

    private TaskStatus status;

    private Date dueDate;

    private Date createdAt;

    private Date completedAt;

    private String metadata;

    private UserLightweightResponseDto assignedBy;

    private UserLightweightResponseDto claimedBy;

    private UserLightweightResponseDto completedBy;

    private Long subscriptionId;

    private String subscriptionServiceName;

    private String subscriptionUsername;

    private Boolean isOverdue;

    private Boolean isClaimed;

    private Integer commentCount;

    private List<TaskCommentResponseDto> comments;
}
