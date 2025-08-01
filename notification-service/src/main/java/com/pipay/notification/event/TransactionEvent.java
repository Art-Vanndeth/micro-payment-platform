package com.pipay.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {
    private String transactionId;
    private String paymentId;
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private String currency;
    private String transactionType;
    private String status;
    private String description;
    private String reference;
    private String transactionReference;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String eventType;
    private String errorMessage;
    private LocalDateTime timestamp;
}
