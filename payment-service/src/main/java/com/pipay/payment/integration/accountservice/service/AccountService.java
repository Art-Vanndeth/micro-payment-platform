package com.pipay.payment.integration.accountservice.service;

import com.pipay.payment.integration.accountservice.dto.BalanceCheckResponse;
import com.pipay.payment.integration.accountservice.dto.AccountValidationResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface AccountService {
    Mono<BalanceCheckResponse> checkBalance(String accountId, BigDecimal amount);

    Mono<AccountValidationResponse> validateRecipientAccount(String recipientAccountId);

    Mono<Boolean> processAccountDeduction(String sourceAccountId, String recipientAccountId, BigDecimal amount);
}
