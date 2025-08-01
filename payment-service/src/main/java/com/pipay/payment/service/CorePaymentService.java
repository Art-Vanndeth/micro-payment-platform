package com.pipay.payment.service;

import com.pipay.payment.integration.accountservice.service.AccountService;
import com.pipay.payment.integration.accountservice.dto.AccountValidationResponse;
import com.pipay.payment.integration.accountservice.dto.AccountStatus;
import com.pipay.payment.exception.CustomException;
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
     * @param recipientAccountId the recipient account ID to validate
     * @return Mono<AccountValidationResponse> validation result
     */
    public Mono<AccountValidationResponse> validateRecipientAccount(String recipientAccountId) {
        log.info("Validating recipient account: {}", recipientAccountId);

        return accountService.validateRecipientAccount(recipientAccountId)
                .doOnSuccess(response -> {
                    if (response.isValid()) {
                        log.info("Recipient account {} is valid with status: {}",
                                recipientAccountId, response.getAccountStatus());
                    } else {
                        log.warn("Recipient account {} validation failed: {}",
                                recipientAccountId, response.getMessage());
                    }
                })
                .doOnError(error -> log.error("Error validating recipient account {}: {}",
                        recipientAccountId, error.getMessage()));
    }

    /**
     * Checks if account status is ACTIVE
     * @param accountStatus the account status to check
     * @return true if account is ACTIVE, false otherwise
     */
    public boolean isAccountActive(AccountStatus accountStatus) {
        return AccountStatus.ACTIVE.equals(accountStatus);
    }

    /**
     * Processes account deduction from source to recipient
     * @param sourceAccountId source account ID
     * @param recipientAccountId recipient account ID
     * @param amount amount to transfer
     * @return Mono<Boolean> success status
     */
    public Mono<Boolean> processAccountDeduction(String sourceAccountId, String recipientAccountId, BigDecimal amount) {
        log.info("Processing account deduction from {} to {} for amount: {}",
                sourceAccountId, recipientAccountId, amount);

        return accountService.processAccountDeduction(sourceAccountId, recipientAccountId, amount)
                .doOnSuccess(success -> {
                    if (success) {
                        log.info("Account deduction processed successfully from {} to {}",
                                sourceAccountId, recipientAccountId);
                    } else {
                        log.error("Account deduction failed from {} to {}",
                                sourceAccountId, recipientAccountId);
                    }
                })
                .doOnError(error -> log.error("Error processing account deduction: {}", error.getMessage()));
    }

    /**
     * Validates both source and recipient accounts before payment processing
     * @param sourceAccountId source account ID
     * @param recipientAccountId recipient account ID
     * @param amount payment amount
     * @return Mono<Void> completes successfully if validation passes
     */
    public Mono<Void> validatePaymentAccounts(String sourceAccountId, String recipientAccountId, BigDecimal amount) {
        log.info("Validating payment accounts - Source: {}, Recipient: {}, Amount: {}",
                sourceAccountId, recipientAccountId, amount);

        // First validate recipient account
        return validateRecipientAccount(recipientAccountId)
                .flatMap(recipientValidation -> {
                    if (!recipientValidation.isValid()) {
                        return Mono.error(new CustomException(INVALID_PARTICIPANT_CODE));
                    }

                    if (!isAccountActive(recipientValidation.getAccountStatus())) {
                        return Mono.error(new CustomException(INVALID_PARTICIPANT_CODE));
                    }

                    // Then check source account balance and status
                    return accountService.checkBalance(sourceAccountId, amount)
                            .flatMap(balanceResponse -> {
                                if (!isAccountActive(balanceResponse.getAccountStatus())) {
                                    return Mono.error(new CustomException(INVALID_PARTICIPANT_CODE));
                                }

                                if (!balanceResponse.isSufficientFunds()) {
                                    return Mono.error(new CustomException(INVALID_PARTICIPANT_CODE));
                                }

                                return Mono.empty();
                            });
                });
    }
}
