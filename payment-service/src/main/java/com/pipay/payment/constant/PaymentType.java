package com.pipay.payment.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentType {
    TRANSFER("Bank Transfer"),
    CARD("Card Payment"),
    WALLET("Digital Wallet"),
    CRYPTO("Cryptocurrency");

    private final String description;
}
