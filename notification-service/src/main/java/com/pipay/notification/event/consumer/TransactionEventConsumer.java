package com.pipay.notification.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipay.notification.dto.NotificationMessage;
import com.pipay.notification.event.TransactionEvent;
import com.pipay.notification.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private final WebSocketNotificationService webSocketNotificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.transaction-events}", groupId = "notification-service-group")
    public void handleTransactionEvent(String transactionEventJson) {
        try {
            log.info("Received transaction event: {}", transactionEventJson);

            TransactionEvent transactionEvent = objectMapper.readValue(transactionEventJson, TransactionEvent.class);

            // Create notification message based on event type
            NotificationMessage notification = createNotificationFromTransactionEvent(transactionEvent);

            // Send real-time notification via WebSocket
            webSocketNotificationService.sendToAdminPortal(notification);

            // Log the notification
            log.info("Real-time notification sent for transaction: {} with status: {}",
                    transactionEvent.getTransactionId(), transactionEvent.getStatus());

        } catch (Exception e) {
            log.error("Error processing transaction event: {}", e.getMessage(), e);
        }
    }

    private NotificationMessage createNotificationFromTransactionEvent(TransactionEvent event) {
        String title = getNotificationTitle(event.getEventType(), event.getStatus());
        String message = getNotificationMessage(event);
        String priority = getNotificationPriority(event.getEventType(), event.getStatus());

        return NotificationMessage.builder()
                .id(java.util.UUID.randomUUID().toString())
                .title(title)
                .message(message)
                .type(mapEventTypeToNotificationType(event.getEventType()))
                .priority(priority)
                .transactionId(event.getTransactionId())
                .paymentId(event.getPaymentId())
                .fromAccountId(event.getFromAccountId())
                .toAccountId(event.getToAccountId())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .status(event.getStatus())
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();
    }

    private String getNotificationTitle(String eventType, String status) {
        return switch (eventType) {
            case "TRANSACTION_CREATED" -> "🆕 New Transaction Created";
            case "TRANSACTION_COMPLETED" -> "✅ Transaction Completed";
            case "TRANSACTION_FAILED" -> "❌ Transaction Failed";
            case "TRANSACTION_STATUS_UPDATED" -> "🔄 Transaction Status Updated";
            default -> "📄 Transaction Event";
        };
    }

    private String getNotificationMessage(TransactionEvent event) {
        String baseMessage = String.format("Transaction %s from account %s to %s for %s %s",
                event.getTransactionId(),
                maskAccountId(event.getFromAccountId()),
                maskAccountId(event.getToAccountId()),
                event.getAmount(),
                event.getCurrency());

        return switch (event.getEventType()) {
            case "TRANSACTION_CREATED" -> baseMessage + " has been initiated";
            case "TRANSACTION_COMPLETED" -> baseMessage + " has been completed successfully";
            case "TRANSACTION_FAILED" -> baseMessage + " has failed" +
                    (event.getErrorMessage() != null ? ": " + event.getErrorMessage() : "");
            case "TRANSACTION_STATUS_UPDATED" -> baseMessage + " status updated to " + event.getStatus();
            default -> baseMessage + " - " + event.getEventType();
        };
    }

    private String getNotificationPriority(String eventType, String status) {
        return switch (eventType) {
            case "TRANSACTION_FAILED" -> "HIGH";
            case "TRANSACTION_CREATED" -> "LOW";
            default -> "MEDIUM";
        };
    }

    private String mapEventTypeToNotificationType(String eventType) {
        return switch (eventType) {
            case "TRANSACTION_CREATED" -> "TRANSACTION_CREATED";
            case "TRANSACTION_COMPLETED" -> "TRANSACTION_COMPLETED";
            case "TRANSACTION_FAILED" -> "TRANSACTION_FAILED";
            case "TRANSACTION_STATUS_UPDATED" -> "TRANSACTION_UPDATED";
            default -> "TRANSACTION_EVENT";
        };
    }

    private String maskAccountId(String accountId) {
        if (accountId == null || accountId.length() < 4) {
            return "****";
        }
        return accountId.substring(0, 4) + "****" + accountId.substring(accountId.length() - 4);
    }
}
