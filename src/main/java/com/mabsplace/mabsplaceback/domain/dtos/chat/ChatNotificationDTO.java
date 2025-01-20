package com.mabsplace.mabsplaceback.domain.dtos.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatNotificationDTO {
    private String chatRoomId;
    private Long senderId;
    private String senderName;
    private String message;
    private String type;  // Add this field for TYPING/STOP_TYPING
    private String timestamp;  // Add this field

    public ChatNotificationDTO(String chatRoomId, Long senderId, String senderName, String message) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
    }
}
