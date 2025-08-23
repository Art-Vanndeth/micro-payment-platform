package com.pipay.account.service;

import com.pipay.account.dto.AccountResponse;
import com.pipay.account.dto.BalanceCheckResponse;
import com.pipay.account.dto.BalanceResponse;
import com.pipay.account.dto.AccountValidationResponse;
import com.pipay.account.dto.TransferResponse;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    AccountResponse getAccountDetails(String accountNumber);
    List<AccountResponse> getAccountList();
    BalanceCheckResponse checkBalance(String accountNumber, BigDecimal amount);
    BalanceResponse getBalance(String accountNumber);
    AccountResponse freezeAccount(String accountNumber);
    AccountResponse unfreezeAccount(String accountNumber);
    AccountValidationResponse validateAccount(String accountNumber);
    TransferResponse processTransfer(String sourceAccountNumber, String recipientAccountNumber, BigDecimal amount);
    Long getTotalAccounts();
    Long getTotalVolume();
    Long getActiveAccounts();
}
