package com.pipay.payment.integration.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountStatus {
    ACTIVE("Account is active"),
    INACTIVE("Account is inactive"),
    SUSPENDED("Account is suspended"),
    CLOSED("Account is closed");

    private final String description;
}
