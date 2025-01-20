package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.chat.*;
import com.mabsplace.mabsplaceback.domain.entities.ChatMessage;
import com.mabsplace.mabsplaceback.domain.entities.ChatRoom;
import com.mabsplace.mabsplaceback.domain.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessage) {
        // Save the message
        ChatMessage savedMessage = chatService.saveMessage(chatMessage);

        // Update the DTO with the saved message ID
        chatMessage.setMessageId(savedMessage.getId());

        // Send acknowledgment to sender
        messagingTemplate.convertAndSendToUser(
                String.valueOf(chatMessage.getSenderId()),
                "/queue/messages",
                new MessageStatusDTO("ACK", chatMessage.getMessageId(),
                        chatMessage.getReceiverId(), chatMessage.getSenderId())
        );

        // Send message to recipient
        messagingTemplate.convertAndSendToUser(
                String.valueOf(chatMessage.getReceiverId()),
                "/queue/messages",
                chatMessage
        );

        // Send notification
        sendNotification(chatMessage);
    }

    @MessageMapping("/chat.messageStatus")
    public void handleMessageStatus(@Payload MessageStatusDTO statusUpdate) {
        // Update message status in database
        switch (statusUpdate.getType()) {
            case "DELIVERED":
                chatService.markMessageAsDelivered(statusUpdate.getMessageId());
                break;
            case "READ":
                chatService.markMessageAsRead(statusUpdate.getMessageId());
                break;
        }

        // Forward the status to the original sender
        messagingTemplate.convertAndSendToUser(
                String.valueOf(statusUpdate.getSenderId()),
                "/queue/messages",
                statusUpdate
        );
    }

    @MessageMapping("/chat.typing")
    public void typingNotification(@Payload ChatNotificationDTO notification) {
        messagingTemplate.convertAndSendToUser(
                notification.getChatRoomId(),
                "/queue/typing",
                notification
        );
    }

    private void sendNotification(ChatMessageDTO message) {
        ChatNotificationDTO notification = new ChatNotificationDTO(
                message.getChatRoomId(),
                message.getSenderId(),
                "New message",
                message.getContent()
        );

        messagingTemplate.convertAndSendToUser(
                String.valueOf(message.getReceiverId()),
                "/queue/notifications",
                notification
        );
    }

    @MessageMapping("/chat.messageDelivered")
    public void messageDelivered(@Payload ChatMessageDTO message) {
        // Update message status in database
        chatService.markMessageAsDelivered(message.getMessageId());

        // Send delivery status to original sender
        messagingTemplate.convertAndSendToUser(
                String.valueOf(message.getSenderId()),
                "/queue/messages",
                new ChatMessageResponse("DELIVERED", message.getMessageId())
        );
    }

    @MessageMapping("/chat.messageRead")
    public void messageRead(@Payload ChatMessageDTO message) {
        // Update message status in database
        chatService.markMessageAsRead(message.getMessageId());

        // Send read status to sender
        messagingTemplate.convertAndSendToUser(
                String.valueOf(message.getSenderId()),
                "/queue/messages",
                new ChatMessageResponse("READ", message.getMessageId())
        );
    }

    @MessageMapping("/chat.adminAssigned")
    public void handleAdminAssignment(@Payload ChatRoom chatRoom) {
        // Send status update to the user
        messagingTemplate.convertAndSendToUser(
                String.valueOf(chatRoom.getUser().getId()),
                "/queue/chat.status",
                new ChatStatusDTO(
                        "ADMIN_ASSIGNED",
                        chatRoom.getChatId(),
                        chatRoom.getAdmin().getId(),
                        chatRoom.getStatus()
                )
        );
    }
}