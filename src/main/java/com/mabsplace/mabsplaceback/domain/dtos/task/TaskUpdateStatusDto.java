package com.mabsplace.mabsplaceback.domain.dtos.task;

import com.mabsplace.mabsplaceback.domain.enums.TaskStatus;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TaskUpdateStatusDto implements Serializable {

    private TaskStatus status;
}
