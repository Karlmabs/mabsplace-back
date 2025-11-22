package com.mabsplace.mabsplaceback.domain.dtos.task;

import com.mabsplace.mabsplaceback.domain.dtos.user.UserLightweightResponseDto;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TaskCommentResponseDto implements Serializable {

    private Long id;

    private Long taskId;

    private UserLightweightResponseDto user;

    private String content;

    private Date createdAt;
}
