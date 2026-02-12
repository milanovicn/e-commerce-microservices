package com.example.notification.controller;

import com.example.notification.dto.NotificationResponse;
import com.example.notification.dto.SendNotificationRequest;
import com.example.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Notification Management", description = "APIs for sending and viewing notifications")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @PostMapping("/send")
    @Operation(summary = "Send notification", 
               description = "Send a notification (typically called by Order Service)")
    public ResponseEntity<NotificationResponse> sendNotification(
            @RequestBody SendNotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping({"", "/"})
    @Operation(summary = "Get all notifications", description = "Retrieve all notifications")
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }
    
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get notifications by order", 
               description = "Retrieve all notifications for a specific order")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByOrder(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(notificationService.getNotificationsByOrder(orderId));
    }
    
    @GetMapping("/email/{email}")
    @Operation(summary = "Get notifications by email", 
               description = "Retrieve all notifications sent to a specific email")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByEmail(
            @PathVariable String email) {
        return ResponseEntity.ok(notificationService.getNotificationsByEmail(email));
    }
}
