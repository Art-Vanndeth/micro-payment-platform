package com.pipay.notification.event.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipay.notification.entity.Notification;
import com.pipay.notification.event.TransactionEvent;
import com.pipay.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @KafkaListener(topics = "${kafka.topics.transaction-created-events-topic}", groupId = "notification-service-group")
    public void handleMessage(@Payload String message,
                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            if (topic.equals("transaction-created-events-topic")) {
                TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);
                handleTransactionEvent(event);
            } else {
                log.warn("Unknown topic: {}", topic);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Message processing failed", e);
        }
    }

    public void handleTransactionEvent(TransactionEvent event) {
        Notification notification = buildNotification(
                event.getTransactionId(),
                "Transaction " + event.getEventType() + ": " + event.getStatus(),
                event.getStatus(),
                System.currentTimeMillis(),
                event.getEventType(),
                event.getToAccountNumber()
        );

        saveAndSendNotification(notification);
    }

    private Notification buildNotification(String transactionId, String message, String status, long timestamp, String type, String recipient) {
        return Notification.builder()
                .transactionId(transactionId)
                .message(message)
                .status(status)
                .isRead(false)
                .timestamp(timestamp)
                .type(type)
                .recipient(recipient)
                .build();
    }

    private void saveAndSendNotification(Notification notification) {

        notification = notificationRepository.save(notification);
        simpMessagingTemplate.convertAndSend(
                "/topic/notifications/",
                notification
        );
    }

}

