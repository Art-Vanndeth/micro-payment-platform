package com.pipay.transaction.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionStatus {
    INITIATED("Transaction initiated"),
    PROCESSING("Transaction in progress"),
    COMPLETED("Transaction completed"),
    FAILED("Transaction failed"),
    CANCELLED("Transaction cancelled"),
    REFUNDED("Transaction refunded");

    private final String description;
}
