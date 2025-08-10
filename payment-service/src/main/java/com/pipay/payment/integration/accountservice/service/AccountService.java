package com.pipay.payment.integration.accountservice.service;

import com.pipay.payment.integration.accountservice.dto.BalanceCheckResponse;
import com.pipay.payment.integration.accountservice.dto.AccountValidationResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface AccountService {
    Mono<BalanceCheckResponse> checkBalance(String accountNumber, BigDecimal amount);

    Mono<AccountValidationResponse> validateRecipientAccount(String recipientAccountNumber);

    Mono<Boolean> processAccountDeduction(String sourceAccountNumber, String recipientAccountNumber, BigDecimal amount);
}
