package com.pipay.payment.dto;

import com.pipay.payment.constant.PaymentMethod;
import com.pipay.payment.constant.PaymentStatus;
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
public class PaymentResponse {

    private String paymentId;
    private String accountId;
    private String recipientAccountId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String description;
    private String reference;
    private String transactionReference;
    private String gatewayResponse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
}
