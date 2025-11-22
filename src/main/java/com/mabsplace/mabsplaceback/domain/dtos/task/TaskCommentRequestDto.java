package com.mabsplace.mabsplaceback.domain.dtos.task;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TaskCommentRequestDto implements Serializable {

    private Long taskId;

    private String content;
}
