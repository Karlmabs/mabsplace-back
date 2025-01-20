package com.mabsplace.mabsplaceback.domain.dtos.chat;

import com.mabsplace.mabsplaceback.domain.enums.ChatStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatRoomDTO {
    private String chatId;
    private Long userId;
    private Long adminId;
    private LocalDateTime createdAt;
    private ChatStatus status;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private String username;
}
