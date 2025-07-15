//package com.pipay.payment.integration.accountservice.service;
//
//import com.pipay.payment.integration.accountservice.dto.AccountStatus;
//import com.pipay.payment.integration.accountservice.dto.BalanceCheckResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.common.Endpoint;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//import java.math.BigDecimal;
//import java.time.Duration;
//
//import static com.pipay.payment.integration.accountservice.endpoint.AccountEndpoint.CHECK_BALANCE;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class AccountServiceImpl implements AccountService {
//
//    private final WebClient webClient;
//
//    @Value("${account-service.url}")
//    private String accountServiceUrl;
//
//    @Override
//    @Cacheable(value = "balanceCheck", key = "#accountId")
//    public Mono<BalanceCheckResponse> checkBalance(String accountId, BigDecimal amount) {
//        log.info("Checking balance for account: {} with amount: {}", accountId, amount);
//
//        return webClient
//                .get()
//                .uri(accountServiceUrl + accountId + CHECK_BALANCE)
//                .retrieve()
//                .bodyToMono(BalanceCheckResponse.class)
//                .map(response -> {
//                    // Check if sufficient funds
//                    boolean sufficientFunds = response.getAvailableBalance().compareTo(amount) >= 0;
//                    return BalanceCheckResponse.builder()
//                            .accountId(response.getAccountId())
//                            .availableBalance(response.getAvailableBalance())
//                            .currentBalance(response.getCurrentBalance())
//                            .sufficientFunds(sufficientFunds)
//                            .currency(response.getCurrency())
//                            .accountStatus(response.getAccountStatus())
//                            .build();
//                })
//                .timeout(Duration.ofSeconds(5))
//                .doOnSuccess(response -> log.info("Balance check successful for account: {}", accountId))
//                .doOnError(error -> log.error("Balance check failed for account: {}", accountId, error))
//                .onErrorReturn(BalanceCheckResponse.builder()
//                        .accountId(accountId)
//                        .availableBalance(BigDecimal.ZERO)
//                        .currentBalance(BigDecimal.ZERO)
//                        .sufficientFunds(false)
//                        .currency("USD")
//                        .accountStatus(AccountStatus.INACTIVE)
//                        .build());
//    }
//}
