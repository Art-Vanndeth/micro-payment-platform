package com.pipay.payment.integration.accountservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferResponse {
    private String transactionId;
    private String sourceAccountId;
    private String recipientAccountId;
    private BigDecimal amount;
    private String currency;
    private boolean success;
    private String status;
    private String message;
    private String errorCode;
    private LocalDateTime processedAt;
}
