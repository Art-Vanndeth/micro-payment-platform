package com.pipay.payment.service;

import com.pipay.payment.exception.CustomException;
import com.pipay.payment.integration.accountservice.dto.AccountStatus;
import com.pipay.payment.integration.accountservice.dto.AccountValidationResponse;
import com.pipay.payment.integration.accountservice.dto.BalanceCheckResponse;
import com.pipay.payment.integration.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static com.pipay.payment.constant.error.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CorePaymentService {

    private final AccountService accountService;

    /**
     * Validates recipient account existence and status
     *
     * @param recipientAccountNumber the recipient account ID to validate
     * @return Mono<AccountValidationResponse> validation result
     */
    public Mono<AccountValidationResponse> validateRecipientAccount(String recipientAccountNumber) {
        log.info("Validating recipient account: {}", recipientAccountNumber);

        return accountService.validateRecipientAccount(recipientAccountNumber)
                .doOnSuccess(response -> {
                    if (response.isValid()) {
                        log.info("Recipient account {} is valid with status: {}",
                                recipientAccountNumber, response.getAccountStatus());
                    } else {
                        log.warn("Recipient account {} validation failed: {}",
                                recipientAccountNumber, response.getMessage());
                    }
                })
                .doOnError(error -> log.error("Error validating recipient account {}: {}",
                        recipientAccountNumber, error.getMessage()));
    }

    /**
     * Checks if account status is ACTIVE
     *
     * @param accountStatus the account status to check
     * @return true if account is ACTIVE, false otherwise
     */
    public boolean isAccountInactive(AccountStatus accountStatus) {
        return !AccountStatus.ACTIVE.equals(accountStatus);
    }

    /**
     * Processes account deduction from source to recipient
     *
     * @param sourceAccountNumber    source account ID
     * @param recipientAccountNumber recipient account ID
     * @param amount                 amount to transfer
     * @return Mono<Boolean> success status
     */
    public Mono<Boolean> processAccountDeduction(String sourceAccountNumber, String recipientAccountNumber, BigDecimal amount) {
        log.info("Processing account deduction from {} to {} for amount: {}",
                sourceAccountNumber, recipientAccountNumber, amount);

        return accountService.processAccountDeduction(sourceAccountNumber, recipientAccountNumber, amount)
                .doOnSuccess(success -> {
                    if (success) {
                        log.info("Account deduction processed successfully from {} to {}",
                                sourceAccountNumber, recipientAccountNumber);
                    } else {
                        log.error("Account deduction failed from {} to {}",
                                sourceAccountNumber, recipientAccountNumber);
                    }
                })
                .doOnError(error -> log.error("Error processing account deduction: {}", error.getMessage()));
    }

    /**
     * Validates both source and recipient accounts before payment processing
     *
     * @param sourceAccountNumber    source account ID
     * @param recipientAccountNumber recipient account ID
     * @param amount                 payment amount
     * @return Mono<Void> completes successfully if validation passes
     */
    public Mono<Void> validatePaymentAccounts(String sourceAccountNumber, String recipientAccountNumber, BigDecimal amount) {
        log.info("Validating payment accounts - Source: {}, Recipient: {}, Amount: {}",
                sourceAccountNumber, recipientAccountNumber, amount);

        return validateRecipientAccount(recipientAccountNumber)
                .flatMap(recipientValidation -> handleRecipientValidation(recipientValidation, recipientAccountNumber))
                .then(accountService.checkBalance(sourceAccountNumber, amount))
                .flatMap(balanceResponse -> handleSourceAccountValidation(balanceResponse, sourceAccountNumber));
    }

    private Mono<Void> handleRecipientValidation(AccountValidationResponse recipientValidation, String recipientAccountNumber) {
        if (!recipientValidation.isValid()) {
            log.warn("Recipient account {} is invalid", recipientAccountNumber);
            return Mono.error(new CustomException(RECIPIENT_ACCOUNT_INVALID));
        }

        if (isAccountInactive(recipientValidation.getAccountStatus())) {
            log.warn("Recipient account {} is inactive", recipientAccountNumber);
            return Mono.error(new CustomException(RECIPIENT_ACCOUNT_INACTIVE));
        }

        return Mono.empty();
    }

    private Mono<Void> handleSourceAccountValidation(BalanceCheckResponse balanceResponse, String sourceAccountNumber) {
        if (isAccountInactive(balanceResponse.getAccountStatus())) {
            log.warn("Source account {} is inactive", sourceAccountNumber);
            return Mono.error(new CustomException(SOURCE_ACCOUNT_INACTIVE));
        }

        if (!balanceResponse.isSufficientFunds()) {
            log.warn("Source account {} has insufficient funds", sourceAccountNumber);
            return Mono.error(new CustomException(INSUFFICIENT_FUNDS));
        }

        return Mono.empty();
    }
}
