package com.pipay.account.service;

import com.pipay.account.dto.AccountResponse;
import com.pipay.account.dto.BalanceCheckResponse;
import com.pipay.account.dto.BalanceResponse;

import java.math.BigDecimal;

public interface AccountService {

    BalanceCheckResponse checkBalance(String accountId, BigDecimal amount);
    BalanceResponse getBalance(String accountId);
    AccountResponse freezeAccount(String accountId);
    AccountResponse unfreezeAccount(String accountId);

}
