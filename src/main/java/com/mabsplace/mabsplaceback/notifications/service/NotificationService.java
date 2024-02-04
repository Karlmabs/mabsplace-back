package com.mabsplace.mabsplaceback.notifications.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  public void sendGlobalNotification(String message) {
    messagingTemplate.convertAndSend("/topic/notifications", message);
  }

  public void sendNotificationToUser(String username, String message) {
    messagingTemplate.convertAndSendToUser(username, "/queue/notifications", message);
  }
}
