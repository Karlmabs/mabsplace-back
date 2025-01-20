package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.ChatMessage;
import com.mabsplace.mabsplaceback.domain.enums.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(String chatRoomId);
    List<ChatMessage> findByReceiverIdAndStatus(Long receiverId, MessageStatus status);
}
