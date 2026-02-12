package com.example.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {
    private Long orderId;
    private String recipientEmail;
    private String message;
    private String notificationType;
}
