package com.pipay.payment.service.impl;

import com.pipay.payment.constant.PaymentStatus;
import com.pipay.payment.dto.BalanceCheckResponse;
import com.pipay.payment.dto.PaymentRequest;
import com.pipay.payment.dto.PaymentResponse;
import com.pipay.payment.entity.Payment;
import com.pipay.payment.repository.PaymentRepository;
import com.pipay.payment.service.PaymentService;
import com.pipay.payment.service.external.AccountServiceClient;
import com.pipay.payment.service.external.PaymentGatewayService;
import com.pipay.payment.service.kafka.PaymentEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final AccountServiceClient accountServiceClient;
    private final PaymentGatewayService paymentGatewayService;
    private final PaymentEventPublisher eventPublisher;
    private final PaymentValidationService validationService;

    @Override
    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        log.info("Starting payment processing for account: {}", request.getAccountId());

        // Action 1: Receive Payment Request (handled by controller)
        // Action 2: Validate Request Data (Spring validation + custom validation)
        return validateRequestData(request)
                // Action 3: Generate Payment ID
                .then(generatePaymentId())
                // Action 4: Check Cache for payment rules and limits
                .flatMap(paymentId -> checkCacheForRules(request, paymentId))
                // Insert payment to DB before any further processing
                .flatMap(payment -> paymentRepository.save(payment)
                    .doOnSuccess(saved -> {
                        log.debug("Inserted payment in DB: {}", saved.getPaymentId());
                        saved.markNotNew(); // Mark as existing for future updates
                    })
                    .doOnError(error -> log.error("Failed to insert payment in DB: {}", error.getMessage()))
                )
                // Action 5: Call Account Service - Check balance and status
                .flatMap(payment -> callAccountService(payment, request)
                    .onErrorResume(error -> {
                        log.warn("Account service call failed: {}", error.getMessage());
                        // Update existing payment instead of creating new one
                        payment.setStatus(PaymentStatus.FAILED);
                        payment.setGatewayResponse("FAILED: " + error.getMessage());
                        payment.setUpdatedAt(LocalDateTime.now());
                        payment.markNotNew(); // Mark as existing for update
                        return Mono.just(payment);
                    }))
                // Action 6: Validate Business Rules - Daily limits, fraud detection
                .flatMap(payment -> {
                    if (payment.getStatus() == PaymentStatus.FAILED) {
                        return Mono.just(payment); // Skip further processing if already failed
                    }
                    return validateBusinessRules(payment, request)
                            .onErrorResume(error -> {
                                log.warn("Business rules validation failed: {}", error.getMessage());
                                // Update existing payment instead of creating new one
                                payment.setStatus(PaymentStatus.FAILED);
                                payment.setGatewayResponse("FAILED: " + error.getMessage());
                                payment.setUpdatedAt(LocalDateTime.now());
                                payment.markNotNew(); // Mark as existing for update
                                return Mono.just(payment);
                            });
                })
                // Action 7: Call Payment Gateway - External payment processing
                .flatMap(payment -> {
                    if (payment.getStatus() == PaymentStatus.FAILED) {
                        return Mono.just(payment); // Skip gateway call if already failed
                    }
                    return callPaymentGateway(payment, request)
                            .onErrorResume(error -> {
                                log.warn("Payment gateway call failed: {}", error.getMessage());
                                // Update existing payment instead of creating new one
                                payment.setStatus(PaymentStatus.FAILED);
                                payment.setGatewayResponse("FAILED: Gateway error - " + error.getMessage());
                                payment.setUpdatedAt(LocalDateTime.now());
                                payment.markNotNew(); // Mark as existing for update
                                return Mono.just(payment);
                            });
                })
                // Action 8: Process Gateway Response - Handle success/failure
                .flatMap(this::processGatewayResponse)
                // Action 9: Update Payment Status - In-memory state management
                .flatMap(this::updatePaymentStatus)
                // Action 10: Publish Payment Event - Kafka event for other services
                .flatMap(this::publishPaymentEvent)
                // Action 11: Cache Payment Result - Redis caching for 15 minutes
                .flatMap(this::cachePaymentResult)
                // Action 12: Return Payment Response - API response to client
                .map(this::buildPaymentResponse)
                .doOnSuccess(response -> log.info("Payment processing completed: {}", response.getPaymentId()))
                .doOnError(error -> log.error("Payment processing failed: {}", error.getMessage()));
    }

    // Action 2: Validate Request Data
    private Mono<Void> validateRequestData(PaymentRequest request) {
        return validationService.validatePaymentRequest(request)
                .doOnSuccess(v -> log.debug("Payment request validation passed"));
    }

    // Action 3: Generate Payment ID
    private Mono<String> generatePaymentId() {
        String paymentId = "PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        log.debug("Generated payment ID: {}", paymentId);
        return Mono.just(paymentId);
    }

    // Action 4: Check Cache for payment rules
    private Mono<Payment> checkCacheForRules(PaymentRequest request, String paymentId) {
        String cacheKey = "payment_rules:" + request.getAccountId();
        return redisTemplate.opsForValue().get(cacheKey)
                .cast(String.class)
                .doOnNext(rules -> log.debug("Found cached payment rules for account: {}", request.getAccountId()))
                .switchIfEmpty(Mono.just("default_rules"))
                .map(rules -> Payment.builder()
                        .paymentId(paymentId)
                        .accountId(request.getAccountId())
                        .recipientAccountId(request.getRecipientAccountId())
                        .amount(request.getAmount())
                        .currency(request.getCurrency())
                        .paymentMethod(request.getPaymentMethod())
                        .status(PaymentStatus.PENDING)
                        .description(request.getDescription())
                        .reference(request.getReference())
                        .createdAt(LocalDateTime.now())
                        .build());
    }

    // Action 5: Call Account Service
    private Mono<Payment> callAccountService(Payment payment, PaymentRequest request) {
        return accountServiceClient.checkBalance(request.getAccountId(), request.getAmount())
                .doOnNext(response -> log.debug("Account balance check response: {}", response))
                .filter(BalanceCheckResponse::isSufficientFunds)
                .switchIfEmpty(Mono.error(new RuntimeException("Insufficient funds")))
                .map(response -> {
                    // Update existing payment instead of creating new one
                    payment.setStatus(PaymentStatus.PROCESSING);
                    payment.setUpdatedAt(LocalDateTime.now());
                    payment.markNotNew(); // Mark as existing for update
                    return payment;
                });
    }

    // Action 6: Validate Business Rules
    private Mono<Payment> validateBusinessRules(Payment payment, PaymentRequest request) {
        return validationService.validateDailyLimits(request.getAccountId(), request.getAmount())
                .then(validationService.validateFraudDetection(request))
                .then(Mono.just(payment))
                .doOnSuccess(p -> log.debug("Business rules validation passed for payment: {}", p.getPaymentId()));
    }

    // Action 7: Call Payment Gateway
    private Mono<Payment> callPaymentGateway(Payment payment, PaymentRequest request) {
        return paymentGatewayService.processPayment(request)
                .doOnNext(response -> log.debug("Payment gateway response: {}", response))
                .map(gatewayResponse -> {
                    // Update existing payment instead of creating new one
                    payment.setGatewayResponse(gatewayResponse.getResponseMessage());
                    payment.setGatewayTransactionId(gatewayResponse.getTransactionId());
                    payment.setTransactionReference(gatewayResponse.getTransactionReference());
                    payment.setUpdatedAt(LocalDateTime.now());
                    payment.markNotNew(); // Mark as existing for update
                    return payment;
                });
    }

    // Action 8: Process Gateway Response
    private Mono<Payment> processGatewayResponse(Payment payment) {
        boolean isSuccess = payment.getGatewayResponse() != null &&
                           payment.getGatewayResponse().contains("SUCCESS");

        PaymentStatus newStatus = isSuccess ? PaymentStatus.COMPLETED : PaymentStatus.FAILED;

        // Update existing payment instead of creating new one
        payment.setStatus(newStatus);
        payment.setUpdatedAt(LocalDateTime.now());
        payment.markNotNew(); // Mark as existing for update

        log.debug("Processed gateway response for payment: {} with status: {}",
                 payment.getPaymentId(), payment.getStatus());

        return Mono.just(payment);
    }

    // Action 9: Update Payment Status
    private Mono<Payment> updatePaymentStatus(Payment payment) {
        return paymentRepository.save(payment)
                .doOnSuccess(saved -> log.debug("Payment status updated in database: {}", saved.getPaymentId()));
    }

    // Action 10: Publish Payment Event
    private Mono<Payment> publishPaymentEvent(Payment payment) {
        return eventPublisher.publishPaymentEvent(payment)
                .then(Mono.just(payment))
                .doOnSuccess(p -> log.debug("Payment event published for: {}", p.getPaymentId()));
    }

    // Action 11: Cache Payment Result
    private Mono<Payment> cachePaymentResult(Payment payment) {
        String cacheKey = "payment_result:" + payment.getPaymentId();
        return redisTemplate.opsForValue()
                .set(cacheKey, payment, Duration.ofMinutes(15))
                .then(Mono.just(payment))
                .doOnSuccess(p -> log.debug("Payment result cached for 15 minutes: {}", p.getPaymentId()));
    }

    // Action 12: Return Payment Response
    private PaymentResponse buildPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .accountId(payment.getAccountId())
                .recipientAccountId(payment.getRecipientAccountId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .description(payment.getDescription())
                .reference(payment.getReference())
                .transactionReference(payment.getTransactionReference())
                .gatewayResponse(payment.getGatewayResponse())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .message(payment.getStatus() == PaymentStatus.COMPLETED ?
                        "Payment processed successfully" : "Payment processing failed")
                .build();
    }

    @Override
    public Mono<PaymentResponse> getPayment(String paymentId) {
        String cacheKey = "payment_result:" + paymentId;

        return redisTemplate.opsForValue().get(cacheKey)
                .cast(Payment.class)
                .switchIfEmpty(paymentRepository.findById(paymentId))
                .map(this::buildPaymentResponse)
                .doOnSuccess(response -> log.debug("Retrieved payment: {}", paymentId));
    }
}
