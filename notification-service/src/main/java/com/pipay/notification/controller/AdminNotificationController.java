//package com.pipay.notification.controller;
//
//import com.pipay.notification.dto.NotificationMessage;
//import com.pipay.notification.service.WebSocketNotificationService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.messaging.simp.annotation.SubscribeMapping;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Slf4j
//@RestController
//@RequestMapping("/api/admin")
//@RequiredArgsConstructor
//public class AdminNotificationController {
//
//    private final WebSocketNotificationService webSocketNotificationService;
//
//    /**
//     * REST endpoint to get recent notifications for admin portal
//     */
//    @GetMapping("/notifications/recent")
//    public List<NotificationMessage> getRecentNotifications() {
//        log.info("Admin requesting recent notifications");
//        return webSocketNotificationService.getRecentNotifications();
//    }
//
//    /**
//     * WebSocket endpoint - called when admin subscribes to notifications
//     */
//    @SubscribeMapping("/admin/notifications")
//    public List<NotificationMessage> onSubscribeToNotifications() {
//        log.info("Admin subscribed to real-time notifications");
//        return webSocketNotificationService.getRecentNotifications();
//    }
//
//    /**
//     * WebSocket endpoint - handle admin connection events
//     */
//    @MessageMapping("/admin/connect")
//    @SendTo("/topic/admin/status")
//    public String handleAdminConnect() {
//        log.info("Admin connected to WebSocket");
//        return "Admin connected";
//    }
//
//    /**
//     * Test endpoint to send a test notification
//     */
//    @PostMapping("/notifications/test")
//    public String sendTestNotification() {
//        NotificationMessage testNotification = NotificationMessage.builder()
//                .id(java.util.UUID.randomUUID().toString())
//                .title("🧪 Test Notification")
//                .message("This is a test notification for the admin portal")
//                .type("TEST")
//                .priority("LOW")
//                .timestamp(java.time.LocalDateTime.now())
//                .read(false)
//                .build();
//
//        webSocketNotificationService.sendToAdminPortal(testNotification);
//        return "Test notification sent";
//    }
//
//    /**
//     * Endpoint to mark notifications as read
//     */
//    @PutMapping("/notifications/{notificationId}/read")
//    public String markAsRead(@PathVariable String notificationId) {
//        log.info("Marking notification as read: {}", notificationId);
//        // Implementation would update the notification status
//        return "Notification marked as read";
//    }
//}
