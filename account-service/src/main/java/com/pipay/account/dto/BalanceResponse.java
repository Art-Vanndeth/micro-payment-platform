package com.pipay.account.dto;

import com.pipay.account.constant.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {
    private String accountNumber;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private String currency;
    private AccountStatus status;
    private LocalDateTime lastUpdated;
}
