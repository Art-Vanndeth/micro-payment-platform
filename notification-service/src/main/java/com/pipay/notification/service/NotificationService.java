package com.pipay.notification.service;

import com.pipay.notification.entity.Notification;

import java.util.List;

public interface NotificationService {
    List<Notification> getAllNotifications();
    void markNotificationAsRead(String id, Boolean read);
    void markAllNotificationsAsRead(String id);
    void removeNotification(String id);

}
