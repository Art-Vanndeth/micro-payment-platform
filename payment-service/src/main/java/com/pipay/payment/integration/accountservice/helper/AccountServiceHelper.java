package com.pipay.payment.integration.accountservice.helper;

import com.pipay.payment.dto.BalanceCheckResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

import static com.pipay.payment.integration.accountservice.constant.AccountEndpoint.CHECK_BALANCE;

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
}
