package com.example.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long orderId;
    private String recipientEmail;
    private String message;
    private String notificationType;
    private LocalDateTime sentAt;
}
