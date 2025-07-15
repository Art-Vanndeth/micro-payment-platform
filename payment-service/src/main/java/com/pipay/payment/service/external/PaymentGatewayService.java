package com.pipay.payment.service.external;

import com.pipay.payment.dto.GatewayResponse;
import com.pipay.payment.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentGatewayService {

    public Mono<GatewayResponse> processPayment(PaymentRequest request) {
        log.debug("Processing payment through gateway for amount: {} {}", request.getAmount(), request.getCurrency());

        // Simulate external payment gateway call
        return Mono.delay(Duration.ofMillis(500)) // Simulate network delay
                .then(Mono.fromCallable(() -> {
                    // Simulate gateway processing logic
                    boolean isSuccess = Math.random() > 0.1; // 90% success rate

                    return GatewayResponse.builder()
                            .transactionId("TXN_" + UUID.randomUUID().toString().substring(0, 8))
                            .transactionReference("REF_" + System.currentTimeMillis())
                            .responseMessage(isSuccess ? "SUCCESS: Payment processed" : "FAILED: Payment declined")
                            .responseCode(isSuccess ? "0000" : "1001")
                            .success(isSuccess)
                            .gatewayName(determineGateway(request))
                            .build();
                }))
                .doOnSuccess(response -> log.debug("Gateway response: {} - {}",
                        response.getResponseCode(), response.getResponseMessage()))
                .doOnError(error -> log.error("Gateway processing failed: {}", error.getMessage()));
    }

    private String determineGateway(PaymentRequest request) {
        return switch (request.getPaymentMethod()) {
            case STRIPE -> "STRIPE";
            case PAYPAL -> "PAYPAL";
            case CREDIT_CARD, DEBIT_CARD -> "CARD_PROCESSOR";
            case BANK_TRANSFER -> "BANK_GATEWAY";
            default -> "DEFAULT_GATEWAY";
        };
    }
}
