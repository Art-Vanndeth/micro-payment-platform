package com.pipay.payment.integration.accountservice.service;

import com.pipay.payment.exception.CustomException;
import com.pipay.payment.integration.accountservice.dto.BalanceCheckResponse;
import com.pipay.payment.integration.accountservice.dto.AccountValidationResponse;
import com.pipay.payment.integration.accountservice.dto.AccountStatus;
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
                        // Convert String to AccountStatus enum if needed
                        AccountStatus accountStatus = convertToAccountStatus(response.getAccountStatus());

                        if (response.getAvailableBalance().compareTo(amount) >= 0) {
                            BalanceCheckResponse account = BalanceCheckResponse.builder()
                                    .accountId(response.getAccountId())
                                    .availableBalance(response.getAvailableBalance())
                                    .currentBalance(response.getCurrentBalance())
                                    .sufficientFunds(true)
                                    .currency(response.getCurrency())
                                    .accountStatus(accountStatus)
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
                                    .accountStatus(accountStatus)
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

    @Override
    public Mono<AccountValidationResponse> validateRecipientAccount(String recipientAccountId) {
        log.info("Validating recipient account: {}", recipientAccountId);

        return accountServiceHelper.validateAccount(recipientAccountId)
                .map(response -> {
                    boolean isValid = response.getAccountId() != null &&
                                    response.getAccountStatus() != null;

                    String message = isValid ? "Account is valid" : "Account not found or invalid";

                    return AccountValidationResponse.builder()
                            .accountId(response.getAccountId())
                            .isValid(isValid)
                            .accountStatus(response.getAccountStatus())
                            .accountType(response.getAccountType())
                            .message(message)
                            .build();
                })
                .onErrorReturn(AccountValidationResponse.builder()
                        .accountId(recipientAccountId)
                        .isValid(false)
                        .message("Account validation failed")
                        .errorCode("VALIDATION_ERROR")
                        .build());
    }

    @Override
    public Mono<Boolean> processAccountDeduction(String sourceAccountId, String recipientAccountId, BigDecimal amount) {
        log.info("Processing account deduction from {} to {} for amount: {}",
                sourceAccountId, recipientAccountId, amount);

        return accountServiceHelper.processTransfer(sourceAccountId, recipientAccountId, amount)
                .map(response -> {
                    boolean success = response != null && response.isSuccess();
                    log.info("Account deduction result: {}", success ? "SUCCESS" : "FAILED");
                    return success;
                })
                .onErrorResume(error -> {
                    log.error("Error processing account deduction: {}", error.getMessage());
                    return Mono.just(false);
                });
    }

    /**
     * Converts AccountStatus from the response (which might be a String) to the enum
     */
    private AccountStatus convertToAccountStatus(Object accountStatusObj) {
        if (accountStatusObj == null) {
            return AccountStatus.INACTIVE;
        }

        if (accountStatusObj instanceof AccountStatus) {
            return (AccountStatus) accountStatusObj;
        }

        if (accountStatusObj instanceof String) {
            try {
                return AccountStatus.valueOf((String) accountStatusObj);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown account status: {}, defaulting to INACTIVE", accountStatusObj);
                return AccountStatus.INACTIVE;
            }
        }

        log.warn("Unexpected account status type: {}, defaulting to INACTIVE", accountStatusObj.getClass());
        return AccountStatus.INACTIVE;
    }
}
