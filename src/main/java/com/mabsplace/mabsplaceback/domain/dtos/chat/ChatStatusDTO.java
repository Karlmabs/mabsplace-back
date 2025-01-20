package com.mabsplace.mabsplaceback.domain.dtos.chat;

import com.mabsplace.mabsplaceback.domain.enums.ChatStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatStatusDTO {
    private String type;
    private String chatRoomId;
    private Long adminId;
    private ChatStatus status;
}
