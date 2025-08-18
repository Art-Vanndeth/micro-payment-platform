package com.pipay.account.dto;

import com.pipay.account.constant.AccountStatus;
import com.pipay.account.constant.AccountType;
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
public class AccountResponse {
    private String accountNumber;
    private String accountHolderName;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private AccountStatus status;
    private AccountType type;
    private String currency;
    private LocalDateTime updatedAt;
    private String message;
}
