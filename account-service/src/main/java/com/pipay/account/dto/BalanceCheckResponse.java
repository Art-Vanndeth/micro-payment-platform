package com.pipay.account.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipay.account.constant.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BalanceCheckResponse {
    private String accountNumber;
    private BigDecimal availableBalance;
    private BigDecimal currentBalance;
    private boolean sufficientFunds;
    private String currency;
    private AccountStatus accountStatus;
}