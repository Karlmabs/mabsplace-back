package com.mabsplace.mabsplaceback.domain.dtos.task;

import com.mabsplace.mabsplaceback.domain.enums.TaskPriority;
import com.mabsplace.mabsplaceback.domain.enums.TaskStatus;
import com.mabsplace.mabsplaceback.domain.enums.TaskType;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TaskFilterDto implements Serializable {

    private TaskStatus status;

    private TaskType type;

    private TaskPriority priority;

    private Long claimedById;

    private String search;
}
