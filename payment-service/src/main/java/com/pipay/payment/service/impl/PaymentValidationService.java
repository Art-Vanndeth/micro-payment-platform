package com.pipay.payment.service.impl;

import com.pipay.payment.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentValidationService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private static final BigDecimal DAILY_LIMIT = new BigDecimal("10000.00");
    private static final BigDecimal SINGLE_TRANSACTION_LIMIT = new BigDecimal("5000.00");

    public Mono<Void> validatePaymentRequest(PaymentRequest request) {
        return Mono.fromRunnable(() -> {
            if (request.getAmount().compareTo(SINGLE_TRANSACTION_LIMIT) > 0) {
                throw new RuntimeException("Transaction amount exceeds single transaction limit");
            }
            log.debug("Payment request validation passed for amount: {}", request.getAmount());
        });
    }

    public Mono<Void> validateDailyLimits(String accountId, BigDecimal amount) {
        String dailyKey = "daily_limit:" + accountId + ":" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        return redisTemplate.opsForValue().get(dailyKey)
                .cast(BigDecimal.class)
                .defaultIfEmpty(BigDecimal.ZERO)
                .flatMap(currentAmount -> {
                    BigDecimal newTotal = currentAmount.add(amount);
                    if (newTotal.compareTo(DAILY_LIMIT) > 0) {
                        return Mono.error(new RuntimeException("Daily transaction limit exceeded"));
                    }
                    return redisTemplate.opsForValue().set(dailyKey, newTotal)
                            .then(redisTemplate.expire(dailyKey, java.time.Duration.ofDays(1)))
                            .then();
                })
                .doOnSuccess(v -> log.debug("Daily limit validation passed for account: {}", accountId));
    }

    public Mono<Void> validateFraudDetection(PaymentRequest request) {
        // Simple fraud detection logic
        return Mono.fromRunnable(() -> {
            // Check for suspicious patterns
            if (request.getAmount().compareTo(new BigDecimal("1000")) > 0 &&
                request.getDescription() != null &&
                request.getDescription().toLowerCase().contains("urgent")) {
                log.warn("Potential fraud detected for account: {} with amount: {}",
                        request.getAccountId(), request.getAmount());
                // In production, this would trigger more sophisticated fraud checks
            }
            log.debug("Fraud detection check passed for payment request");
        });
    }
}
