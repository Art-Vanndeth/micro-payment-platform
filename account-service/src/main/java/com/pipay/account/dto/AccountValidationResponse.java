package com.pipay.account.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipay.account.constant.AccountStatus;
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
