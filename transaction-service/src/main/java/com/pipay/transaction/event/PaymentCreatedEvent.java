package com.pipay.transaction.event;

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
public class PaymentCreatedEvent {
    private String paymentId;
    private String accountId;
    private String recipientAccountId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String status;
    private String description;
    private String reference;
    private String transactionReference;
    private LocalDateTime createdAt;
    private String eventType;
}
