package com.pipay.notification.controller;

import com.pipay.notification.entity.Notification;
import com.pipay.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    @PutMapping("/{id}/status")
    public void updateNotificationStatus(@PathVariable String id, @RequestParam Boolean read) {
        notificationService.markNotificationAsRead(id, read);
    }

    @PutMapping("/{id}/mark-all-read")
    public void updateAllNotificationsStatus(@PathVariable String id) {
        notificationService.markAllNotificationsAsRead(id);
    }

    @DeleteMapping("/{id}")
    public void removeNotification(@PathVariable String id) {
        notificationService.removeNotification(id);
    }
}

