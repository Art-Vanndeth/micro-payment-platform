package com.pipay.transaction.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipay.transaction.constant.TransactionStatus;
import com.pipay.transaction.constant.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "transactions")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transaction {

    @Id
    private String transactionId;

    @NotNull
    @NotBlank
    private String paymentId;

    @NotNull
    @NotBlank
    private String paymentMethod;

    @NotNull
    @Indexed
    private String fromAccountNumber;

    @NotNull
    @NotBlank
    private String toAccountNumber;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotNull
    @Size(min = 3, max = 3)
    private String currency;

    @NotNull
    private TransactionType transactionType;

    @NotNull
    private TransactionStatus status;

    private String description;
    private String reference;
    private String transactionReference;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    private String gatewayTransactionId;
    private String gatewayResponse;
    private String failureReason;

    // Audit fields
    private String createdBy;
    private String lastModifiedBy;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
