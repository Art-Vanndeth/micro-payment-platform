package com.pipay.account.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountStatus {
    ACTIVE("Account is active"),
    FROZEN("Account is frozen"),
    SUSPENDED("Account is suspended"),
    CLOSED("Account is closed");

    private final String description;
}
