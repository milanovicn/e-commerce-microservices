package com.example.order.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationClient {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${notification.service.url}")
    private String notificationServiceUrl;
    
    public void sendOrderNotification(NotificationRequest request) {
        log.info("Calling Notification Service for order: {}", request.getOrderId());
        webClientBuilder.build()
                .post()
                .uri(notificationServiceUrl + "/api/notifications/send")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(
                    result -> log.info("Notification sent successfully"),
                    error -> log.error("Failed to send notification: {}", error.getMessage())
                );
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationRequest {
        private Long orderId;
        private String recipientEmail;
        private String message;
        private String notificationType;
    }
}
