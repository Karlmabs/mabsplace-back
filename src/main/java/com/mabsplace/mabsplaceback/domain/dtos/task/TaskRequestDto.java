package com.mabsplace.mabsplaceback.domain.dtos.task;

import com.mabsplace.mabsplaceback.domain.enums.TaskPriority;
import com.mabsplace.mabsplaceback.domain.enums.TaskStatus;
import com.mabsplace.mabsplaceback.domain.enums.TaskType;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TaskRequestDto implements Serializable {

    private String title;

    private String description;

    private TaskType type;

    private TaskPriority priority;

    private TaskStatus status;

    private Date dueDate;

    private String metadata;

    private Long subscriptionId;
}
