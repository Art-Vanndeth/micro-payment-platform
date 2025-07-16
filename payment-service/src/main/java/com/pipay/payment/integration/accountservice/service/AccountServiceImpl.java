package com.pipay.payment.integration.accountservice.service;

import com.pipay.payment.exception.CustomException;
import com.pipay.payment.integration.accountservice.dto.BalanceCheckResponse;
import com.pipay.payment.integration.accountservice.helper.AccountServiceHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static com.pipay.payment.constant.error.ErrorCode.ACCOUNT_NOT_FOUND;


@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountServiceHelper accountServiceHelper;

    @Override
    public Mono<BalanceCheckResponse> checkBalance(String accountId, BigDecimal amount) {
        return accountServiceHelper.checkBalance(accountId, amount)
                .flatMap(response -> {
                    if (response.getAccountId().equals(accountId)) {
                        if (response.getAvailableBalance().compareTo(amount) >= 0) {
                            BalanceCheckResponse account = BalanceCheckResponse.builder()
                                    .accountId(response.getAccountId())
                                    .availableBalance(response.getAvailableBalance())
                                    .currentBalance(response.getCurrentBalance())
                                    .sufficientFunds(true)
                                    .currency(response.getCurrency())
                                    .build();
                            return Mono.just(account);
                        } else {
                            log.warn("Insufficient funds for account: {}", accountId);
                            return Mono.just(BalanceCheckResponse.builder()
                                    .accountId(response.getAccountId())
                                    .availableBalance(response.getAvailableBalance())
                                    .currentBalance(response.getCurrentBalance())
                                    .sufficientFunds(false)
                                    .currency(response.getCurrency())
                                    .build());
                        }
                    } else {
                        log.warn("Account not found for accountId: {}", accountId);
                        return Mono.error(new CustomException(ACCOUNT_NOT_FOUND));
                    }
                })
                .onErrorResume(error -> error instanceof CustomException customException
                        ? Mono.error(customException)
                        : Mono.error(new CustomException(ACCOUNT_NOT_FOUND)));
    }
}
