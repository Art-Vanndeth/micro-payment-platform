package com.pipay.transaction.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionType {
    DEBIT("Money Out"),
    CREDIT("Money In"),
    TRANSFER("Transfer"),
    REFUND("Refund");

    private final String description;
}
