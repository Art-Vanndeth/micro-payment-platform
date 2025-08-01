package com.pipay.payment.integration.accountservice.helper;

import com.pipay.payment.dto.BalanceCheckResponse;
import com.pipay.payment.integration.accountservice.dto.AccountValidationResponse;
import com.pipay.payment.integration.accountservice.dto.TransferResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

import static com.pipay.payment.integration.accountservice.constant.AccountEndpoint.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceHelper {

    private final WebClient webClient;

    @Value("${integration.account-service.url}")
    private String accountServiceUrl;

    public Mono<BalanceCheckResponse> checkBalance(String accountId, BigDecimal amount) {
        log.debug("Calling account service to check balance for account: {} with amount: {}", accountId, amount);

        // Wrap amount in a map to match the expected JSON structure {"amount": value}
        Map<String, BigDecimal> requestBody = Map.of("amount", amount);

        return webClient.post()
                .uri(accountServiceUrl + "/" + accountId + CHECK_BALANCE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(BalanceCheckResponse.class);
    }

    public Mono<AccountValidationResponse> validateAccount(String accountId) {
        log.debug("Calling account service to validate account: {}", accountId);

        return webClient.get()
                .uri(accountServiceUrl + "/" + accountId + VALIDATE_ACCOUNT)
                .retrieve()
                .bodyToMono(AccountValidationResponse.class)
                .doOnSuccess(response -> log.debug("Account validation response for {}: {}", accountId, response))
                .doOnError(error -> log.error("Error validating account {}: {}", accountId, error.getMessage()));
    }

    public Mono<TransferResponse> processTransfer(String sourceAccountId, String recipientAccountId, BigDecimal amount) {
        log.debug("Calling account service to process transfer from {} to {} for amount: {}",
                sourceAccountId, recipientAccountId, amount);

        Map<String, Object> requestBody = Map.of(
                "sourceAccountId", sourceAccountId,
                "recipientAccountId", recipientAccountId,
                "amount", amount
        );

        return webClient.post()
                .uri(accountServiceUrl + PROCESS_TRANSFER)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(TransferResponse.class)
                .doOnSuccess(response -> log.debug("Transfer response: {}", response))
                .doOnError(error -> log.error("Error processing transfer: {}", error.getMessage()));
    }
}
