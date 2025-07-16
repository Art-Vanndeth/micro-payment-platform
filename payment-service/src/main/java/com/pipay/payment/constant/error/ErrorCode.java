package com.pipay.payment.constant.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    SUCCESS("0", "Success"),

    INTERNAL_SERVER_ERROR("999", "Internal Server Error"),
    RECIPIENT_TIMEOUT("998", "Recipient Timeout"),
    SERVICE_UNAVAILABLE("997", "Service is unavailable"),
    SOMETHING_WENT_WRONG("996", "Something went wrong"),

    AUTHENTICATION_ERROR("001", "Authentication error"),
    VALIDATION_ERROR("002", "Validation Error"),
    INVALID_QR_CODE("003", "Invalid QR Code"),
    INVALID_AMOUNT("004", "Invalid amount"),
    INVALID_CURRENCY("005", "Invalid Currency"),
    INVALID_MERCHANT("006", "Invalid Merchant"),
    INVALID_PAYMENT_REFERENCE("007", "Invalid Payment Reference"),
    INVALID_CREDIT_ACCOUNT_NUMBER("008", "Invalid Credit Account Number"),
    INVALID_TRANSACTION("009", "Invalid Transaction"),
    QR_CODE_NOT_AVAILABLE("010", "QR Code is not available for payment"),
    QR_CODE_CURRENCY_NOT_SUPPORTED("011", "QR Code Currency is not support"),
    MERCHANT_OVER_LIMIT("012", "Merchant over limit amount"),
    MERCHANT_NOT_FOUND("012", "Merchant not found"),
    REQUEST_CANNOT_BE_PROCESSED("013", "Request cannot be processed"),
    UNSUPPORTED_COUNTRY("014", "Unsupported Country"),
    ACCOUNT_NOT_FOUND("015", "Account not found"),
    INVALID_PARTICIPANT_CODE("016", "Invalid participant code"),
    QR_EXPIRED("017", "QR Expired"),
    QR_NOT_FOUND("018", "QR not found");

    private final String code;
    private final String message;
}
