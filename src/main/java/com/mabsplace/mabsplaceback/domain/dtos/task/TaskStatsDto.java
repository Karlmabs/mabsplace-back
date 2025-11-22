package com.mabsplace.mabsplaceback.domain.dtos.task;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TaskStatsDto implements Serializable {

    private long todoCount;

    private long inProgressCount;

    private long doneCount;

    private long overdueCount;
}
