package com.pipay.payment.integration.accountservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountValidationResponse {
    private String accountId;
    private boolean isValid;
    private AccountStatus accountStatus;
    private String accountType;
    private String message;
    private String errorCode;
}
