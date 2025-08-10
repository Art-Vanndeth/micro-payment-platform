package com.pipay.transaction.dto;

import com.pipay.transaction.constant.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTransactionRequest {

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
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @NotNull
    private TransactionType transactionType;

    private String description;

    private String gatewayTransactionId;

    private Map<String, Object> metadata;
}
