package com.pipay.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipay.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    // Store recent notifications for new admin connections
    private final ConcurrentHashMap<String, List<NotificationMessage>> recentNotifications = new ConcurrentHashMap<>();
    private static final int MAX_RECENT_NOTIFICATIONS = 50;

    /**
     * Send real-time notification to admin portal
     */
    public void sendToAdminPortal(NotificationMessage notification) {
        try {
            log.info("Sending real-time notification to admin portal: {}", notification.getTitle());

            // Send to all connected admin clients
            messagingTemplate.convertAndSend("/topic/admin/notifications", notification);

            // Store notification for recent history
            storeRecentNotification(notification);

            // Send notification count update
            sendNotificationCountUpdate();

            log.info("Real-time notification sent successfully: {}", notification.getId());

        } catch (Exception e) {
            log.error("Error sending WebSocket notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send notification to specific admin user
     */
    public void sendToSpecificAdmin(String adminId, NotificationMessage notification) {
        try {
            log.info("Sending notification to admin {}: {}", adminId, notification.getTitle());

            messagingTemplate.convertAndSendToUser(adminId, "/queue/notifications", notification);

        } catch (Exception e) {
            log.error("Error sending notification to admin {}: {}", adminId, e.getMessage(), e);
        }
    }

    /**
     * Send transaction alert for high-priority events
     */
    public void sendTransactionAlert(NotificationMessage notification) {
        try {
            if ("HIGH".equals(notification.getPriority())) {
                // Send urgent alert to admin dashboard
                messagingTemplate.convertAndSend("/topic/admin/alerts", notification);
                log.info("High priority alert sent: {}", notification.getTitle());
            }

            // Also send to general notifications
            sendToAdminPortal(notification);

        } catch (Exception e) {
            log.error("Error sending transaction alert: {}", e.getMessage(), e);
        }
    }

    /**
     * Get recent notifications for new admin connections
     */
    public List<NotificationMessage> getRecentNotifications() {
        return new ArrayList<>(recentNotifications.getOrDefault("recent", new ArrayList<>()));
    }

    /**
     * Send dashboard statistics update
     */
    public void sendDashboardUpdate(String updateType, Object data) {
        try {
            DashboardUpdate update = DashboardUpdate.builder()
                    .type(updateType)
                    .data(data)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/admin/dashboard", update);
            log.debug("Dashboard update sent: {}", updateType);

        } catch (Exception e) {
            log.error("Error sending dashboard update: {}", e.getMessage(), e);
        }
    }

    private void storeRecentNotification(NotificationMessage notification) {
        List<NotificationMessage> recent = recentNotifications.computeIfAbsent("recent", k -> new ArrayList<>());

        // Add to beginning of list
        recent.add(0, notification);

        // Keep only recent notifications
        if (recent.size() > MAX_RECENT_NOTIFICATIONS) {
            recent.subList(MAX_RECENT_NOTIFICATIONS, recent.size()).clear();
        }
    }

    private void sendNotificationCountUpdate() {
        try {
            List<NotificationMessage> recent = getRecentNotifications();
            long unreadCount = recent.stream().filter(n -> !n.isRead()).count();

            NotificationCountUpdate countUpdate = NotificationCountUpdate.builder()
                    .totalCount(recent.size())
                    .unreadCount((int) unreadCount)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/admin/notification-count", countUpdate);

        } catch (Exception e) {
            log.error("Error sending notification count update: {}", e.getMessage(), e);
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DashboardUpdate {
        private String type;
        private Object data;
        private java.time.LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NotificationCountUpdate {
        private int totalCount;
        private int unreadCount;
        private java.time.LocalDateTime timestamp;
    }
}
