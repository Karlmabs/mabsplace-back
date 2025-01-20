package com.mabsplace.mabsplaceback.domain.dtos.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatMessageResponse {
    private String type;  // ACK, DELIVERED, READ
    private Long messageId;
    private Long timestamp;

    public ChatMessageResponse(String type, Long messageId) {
        this.type = type;
        this.messageId = messageId;
        this.timestamp = System.currentTimeMillis();
    }
}