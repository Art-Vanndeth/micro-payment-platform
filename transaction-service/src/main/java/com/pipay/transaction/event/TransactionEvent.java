package com.pipay.transaction.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipay.transaction.constant.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionEvent {
    private String eventId;
    private String eventType;
    private String paymentId;
    private String transactionId;
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private LocalDateTime timestamp;
    private Map<String, Object> payload;
}
