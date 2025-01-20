package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.chat.ChatMessageDTO;
import com.mabsplace.mabsplaceback.domain.dtos.chat.ChatRoomDTO;
import com.mabsplace.mabsplaceback.domain.entities.ChatRoom;
import com.mabsplace.mabsplaceback.domain.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomDTO> createChatRoom(@RequestParam Long userId) {
        return ResponseEntity.ok(chatService.createChatRoom(userId));
    }

    @PutMapping("/rooms/{chatId}/assign")
    public ResponseEntity<ChatRoom> assignAdminToChat(
            @PathVariable String chatId,
            @RequestParam Long adminId) {
        return ResponseEntity.ok(chatService.assignAdminToChat(chatId, adminId));
    }

    @GetMapping("/rooms/active")
    public ResponseEntity<List<ChatRoomDTO>> getActiveChats() {
        return ResponseEntity.ok(chatService.getActiveChats());
    }

    // Add a new endpoint to get all chats
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDTO>> getAllChats() {
        return ResponseEntity.ok(chatService.getAllChats());
    }

    @GetMapping("/rooms/pending")
    public ResponseEntity<List<ChatRoomDTO>> getPendingChats() {
        return ResponseEntity.ok(chatService.getPendingChats());
    }

    @GetMapping("/messages/{chatRoomId}")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(
            @PathVariable String chatRoomId) {
        return ResponseEntity.ok(chatService.getChatHistory(chatRoomId));
    }

    @PutMapping("/messages/mark-delivered")
    public ResponseEntity<Void> markMessagesAsDelivered(@RequestParam Long receiverId) {
        chatService.markMessagesAsDelivered(receiverId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/messages/mark-read")
    public ResponseEntity<Void> markMessagesAsRead(
            @RequestParam String chatRoomId,
            @RequestParam Long userId) {
        chatService.markMessagesAsRead(chatRoomId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/rooms/{chatId}/close")
    public ResponseEntity<Void> closeChatRoom(@PathVariable String chatId) {
        chatService.closeChatRoom(chatId);
        return ResponseEntity.ok().build();
    }
}
