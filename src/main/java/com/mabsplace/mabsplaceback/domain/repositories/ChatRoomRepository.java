package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.ChatRoom;
import com.mabsplace.mabsplaceback.domain.enums.ChatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByChatId(String chatId);
    List<ChatRoom> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<ChatRoom> findByStatus(ChatStatus status);
}
