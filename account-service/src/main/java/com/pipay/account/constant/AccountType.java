package com.pipay.account.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountType {
    SAVINGS("Savings Account"),
    CURRENT("Current Account"),
    BUSINESS("Business Account"),
    JOINT("Joint Account");

    private final String description;
}
