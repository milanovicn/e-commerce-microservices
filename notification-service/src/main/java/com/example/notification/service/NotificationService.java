package com.example.notification.service;

import com.example.notification.dto.NotificationResponse;
import com.example.notification.dto.SendNotificationRequest;
import com.example.notification.model.Notification;
import com.example.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    @Transactional
    public NotificationResponse sendNotification(SendNotificationRequest request) {
        log.info("Sending {} notification for order: {} to: {}", 
                 request.getNotificationType(), 
                 request.getOrderId(), 
                 request.getRecipientEmail());
        
        Notification notification = new Notification();
        notification.setOrderId(request.getOrderId());
        notification.setRecipientEmail(request.getRecipientEmail());
        notification.setMessage(request.getMessage());
        notification.setNotificationType(request.getNotificationType());
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Simulate email sending (in real app, use JavaMailSender or external service)
        log.info("✉️  EMAIL SENT to {}: {}", 
                 request.getRecipientEmail(), 
                 request.getMessage());
        
        return convertToResponse(savedNotification);
    }
    
    public List<NotificationResponse> getAllNotifications() {
        log.info("Fetching all notifications");
        return notificationRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<NotificationResponse> getNotificationsByOrder(Long orderId) {
        log.info("Fetching notifications for order: {}", orderId);
        return notificationRepository.findByOrderId(orderId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<NotificationResponse> getNotificationsByEmail(String email) {
        log.info("Fetching notifications for email: {}", email);
        return notificationRepository.findByRecipientEmail(email).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    private NotificationResponse convertToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getOrderId(),
                notification.getRecipientEmail(),
                notification.getMessage(),
                notification.getNotificationType(),
                notification.getSentAt()
        );
    }
}
