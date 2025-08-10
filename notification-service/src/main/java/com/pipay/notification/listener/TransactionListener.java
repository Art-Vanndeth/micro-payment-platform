//package com.pipay.notification.listener;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.pipay.notification.entity.Notification;
//import com.pipay.notification.service.NotificationService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Component
//public class TransactionListener {
//
//    @Autowired
//    private NotificationService notificationService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @KafkaListener(topics = "transaction-events", groupId = "notification-group")
//    public void listen(String message) {
//        try {
//            Notification notification = objectMapper.readValue(message, Notification.class);
//            notificationService.saveNotification(notification);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
