package com.pipay.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceCheckResponse {
    private String accountNumber;
    private BigDecimal availableBalance;
    private BigDecimal currentBalance;
    private boolean sufficientFunds;
    private String currency;
    private String accountStatus;
}
