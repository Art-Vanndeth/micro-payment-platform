package com.pipay.transaction.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentEvent {
    private String paymentId;
    private String accountNumber;
    private String recipientAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String status;
    private String description;
    private String reference;
    private String transactionReference;
    private LocalDateTime createdAt;
    private String eventType; // PAYMENT_CREATED, PAYMENT_COMPLETED, etc.
}
