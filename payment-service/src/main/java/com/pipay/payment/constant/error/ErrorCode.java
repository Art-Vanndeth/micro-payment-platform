package com.pipay.payment.constant.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    SUCCESS("0", "Success"),
    VALIDATION_ERROR("002", "Validation Error"),
    ACCOUNT_NOT_FOUND("015", "Account not found"),
    INVALID_PARTICIPANT_CODE("016", "Invalid participant code"),
    RECIPIENT_ACCOUNT_INVALID("017", "Recipient account is invalid"),
    RECIPIENT_ACCOUNT_INACTIVE("018", "Recipient account is inactive"),
    SOURCE_ACCOUNT_INACTIVE("019", "Source account is inactive"),
    INSUFFICIENT_FUNDS("020", "Insufficient funds"),
    UNSUPPORTED_CURRENCY("021", "Unsupported currency");

    private final String code;
    private final String message;
}
