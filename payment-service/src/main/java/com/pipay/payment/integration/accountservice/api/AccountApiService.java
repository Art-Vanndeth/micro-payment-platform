//package com.pipay.payment.integration.accountservice.api;
//
//import com.pipay.payment.integration.accountservice.dto.BalanceCheckResponse;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//import java.math.BigDecimal;
//
//import static com.pipay.payment.integration.accountservice.endpoint.AccountEndpoint.CHECK_BALANCE;
//
//@Service
//public class AccountApiService {
//    WebClient webClient;
//
//    @Value("${integration.api.account.base-url}")
//    private String url;
//
//    public Mono<BalanceCheckResponse> checkBalance(String accountNumber, BigDecimal amount) {
//
//        return webClient.post()
//                .uri(url + accountNumber + CHECK_BALANCE)
//                .header("Content-Type", "application/json")
//                .bodyValue(amount)
//                .retrieve()
//                .bodyToMono(BalanceCheckResponse.class);
//    }
//}
