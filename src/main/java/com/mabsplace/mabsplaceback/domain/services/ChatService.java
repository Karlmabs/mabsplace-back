package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.chat.ChatMessageDTO;
import com.mabsplace.mabsplaceback.domain.dtos.chat.ChatRoomDTO;
import com.mabsplace.mabsplaceback.domain.dtos.chat.ChatStatusDTO;
import com.mabsplace.mabsplaceback.domain.entities.ChatMessage;
import com.mabsplace.mabsplaceback.domain.entities.ChatRoom;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.enums.ChatStatus;
import com.mabsplace.mabsplaceback.domain.enums.MessageStatus;
import com.mabsplace.mabsplaceback.domain.enums.MessageType;
import com.mabsplace.mabsplaceback.domain.repositories.ChatMessageRepository;
import com.mabsplace.mabsplaceback.domain.repositories.ChatRoomRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository messageRepository;
    private final ChatRoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMessage saveMessage(ChatMessageDTO messageDTO) {
        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(messageDTO.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        ChatMessage message = new ChatMessage();
        message.setContent(messageDTO.getContent());
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SENT);
        message.setChatRoomId(messageDTO.getChatRoomId());

        return messageRepository.save(message);
    }

    @Transactional
    public ChatRoomDTO createChatRoom(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatId(generateUniqueChatId());
        chatRoom.setUser(user);
        chatRoom.setCreatedAt(LocalDateTime.now());
        chatRoom.setStatus(ChatStatus.PENDING);

        roomRepository.save(chatRoom);
        return convertToChatRoomDTO(chatRoom);
    }

    @Transactional
    public ChatRoom assignAdminToChat(String chatId, Long adminId) {
        ChatRoom chatRoom = roomRepository.findByChatId(chatId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        chatRoom.setAdmin(admin);
        chatRoom.setStatus(ChatStatus.ACTIVE);

        // Send WebSocket notification to the user
        ChatStatusDTO statusUpdate = new ChatStatusDTO(
                "ADMIN_ASSIGNED",
                chatId,
                adminId,
                ChatStatus.ACTIVE
        );

        log.info("Sending status update to user: {}", statusUpdate);

        // Send to the user's queue
        messagingTemplate.convertAndSendToUser(
                String.valueOf(chatRoom.getUser().getId()),
                "/queue/chat.status",
                statusUpdate
        );

        log.info("Status update sent to user: {}", statusUpdate);

        return roomRepository.save(chatRoom);
    }

    public List<ChatMessageDTO> getChatHistory(String chatRoomId) {
        List<ChatMessage> messages = messageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId);
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ChatRoomDTO> getActiveChats() {
        return roomRepository.findByStatus(ChatStatus.ACTIVE)
                .stream()
                .map(this::convertToChatRoomDTO)
                .collect(Collectors.toList());
    }

    public List<ChatRoomDTO> getPendingChats() {
        return roomRepository.findByStatus(ChatStatus.PENDING)
                .stream()
                .map(this::convertToChatRoomDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsDelivered(Long receiverId) {
        List<ChatMessage> undeliveredMessages = messageRepository
                .findByReceiverIdAndStatus(receiverId, MessageStatus.SENT);

        undeliveredMessages.forEach(message -> {
            message.setStatus(MessageStatus.DELIVERED);
            messageRepository.save(message);
        });
    }

    @Transactional
    public void markMessagesAsRead(String chatRoomId, Long userId) {
        List<ChatMessage> messages = messageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId);
        messages.stream()
                .filter(message -> message.getReceiver().getId().equals(userId))
                .forEach(message -> {
                    message.setStatus(MessageStatus.READ);
                    messageRepository.save(message);
                });
    }

    @Transactional
    public void closeChatRoom(String chatId) {
        ChatRoom chatRoom = roomRepository.findByChatId(chatId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        chatRoom.setStatus(ChatStatus.CLOSED);
        roomRepository.save(chatRoom);
    }

    private String generateUniqueChatId() {
        // Simple implementation - in production, use more sophisticated method
        return "CHAT_" + System.currentTimeMillis();
    }

    private ChatMessageDTO convertToDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setContent(message.getContent());
        dto.setSenderId(message.getSender().getId());
        dto.setReceiverId(message.getReceiver().getId());
        dto.setChatRoomId(message.getChatRoomId());
        dto.setTimestamp(message.getTimestamp());
        dto.setType(MessageType.CHAT);
        return dto;
    }

    private ChatRoomDTO convertToChatRoomDTO(ChatRoom chatRoom) {
        ChatRoomDTO dto = new ChatRoomDTO();
        dto.setChatId(chatRoom.getChatId());
        dto.setUserId(chatRoom.getUser().getId());
        dto.setUsername(chatRoom.getUser().getUsername());
        dto.setAdminId(chatRoom.getAdmin() != null ? chatRoom.getAdmin().getId() : null);
        dto.setCreatedAt(chatRoom.getCreatedAt());
        dto.setStatus(chatRoom.getStatus());

        // Get last message if exists
        Optional<ChatMessage> lastMessage = messageRepository
                .findByChatRoomIdOrderByTimestampAsc(chatRoom.getChatId())
                .stream()
                .reduce((first, second) -> second);

        lastMessage.ifPresent(message -> {
            dto.setLastMessage(message.getContent());
            dto.setLastMessageTime(message.getTimestamp());
        });

        return dto;
    }

    public void markMessageAsRead(Long messageId) {
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setStatus(MessageStatus.READ);
        messageRepository.save(message);
    }

    public void markMessageAsDelivered(Long messageId) {
        chatMessageRepository.findById(messageId).ifPresent(message -> {
            message.setStatus(MessageStatus.DELIVERED);
            chatMessageRepository.save(message);
        });
    }

    public List<ChatRoomDTO> getAllChats() {
        return roomRepository.findAll()
                .stream()
                .map(this::convertToChatRoomDTO)
                .collect(Collectors.toList());
    }
}
