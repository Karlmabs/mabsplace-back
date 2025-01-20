package com.mabsplace.mabsplaceback.domain.dtos.chat;

import com.mabsplace.mabsplaceback.domain.enums.MessageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageDTO {
    private String content;
    private Long senderId;
    private Long receiverId;
    private String chatRoomId;
    private LocalDateTime timestamp;
    private MessageType type;
    private Long messageId;
}
