package com.mabsplace.mabsplaceback.domain.dtos.chat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatusDTO {
    private String type;  // ACK, DELIVERED, READ
    private Long messageId;
    private Long senderId;
    private Long receiverId;
}