package com.mabsplace.mabsplaceback.notifications.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationController {

  @MessageMapping("/notify") // Endpoint to send notifications
  @SendTo("/topic/notifications") // Topic to which the notification will be sent
  public String send(String notification) {
    return notification; // Simply return the notification text
  }
}
