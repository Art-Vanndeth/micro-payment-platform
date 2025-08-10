package com.pipay.payment.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipay.payment.constant.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class PaymentRequest {
    @NotNull
    @NotBlank
    private String paymentId;

    @NotNull
    @NotBlank
    private String fromAccountNumber;

    @NotNull
    @NotBlank
    private String toAccountNumber;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull
    @Size(min = 3, max = 3)
    private String currency;

    private String description;

    @NotNull
    private PaymentType paymentType;

    private LocalDateTime createdAt;

    @NotNull
    private String clientRequestId;
}
