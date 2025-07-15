package com.pipay.payment.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipay.payment.constant.PaymentStatus;
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
public class PaymentResponse {
    private String paymentId;
    private PaymentStatus status;
    private String message;
    private BigDecimal amount;
    private String currency;
    private String transactionId;
    private LocalDateTime processedAt;
    private String gatewayResponse;
    private Map<String, Object> metadata;
}
