package com.pipay.payment.service;

import com.pipay.payment.dto.PaymentRequest;
import com.pipay.payment.dto.PaymentResponse;
import com.pipay.payment.entity.Payment;
import com.pipay.payment.event.PaymentCreatedEvent;
import com.pipay.payment.exception.CustomException;
import com.pipay.payment.integration.accountservice.service.AccountService;
import com.pipay.payment.repository.PaymentRepository;
import com.pipay.payment.constant.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.pipay.payment.constant.error.ErrorCode.INVALID_PARTICIPANT_CODE;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${kafka.topics.payment-created-events}")
    private String paymentCreatedTopic;

    private final PaymentRepository paymentRepository;
    private final AccountService accountService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Mono<PaymentResponse> payment(PaymentRequest request) {
        return accountService.checkBalance(request.getAccountId(), request.getAmount())
                .flatMap(response -> {
                    log.info("Checking balance for account: {}", response);
                    if (response.isSufficientFunds()) {
                        // Create payment entity
                        String paymentId = UUID.randomUUID().toString();
                        String transactionReference = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                        Payment payment = Payment.builder()
                                .paymentId(paymentId)
                                .accountId(request.getAccountId())
                                .recipientAccountId(request.getRecipientAccountId())
                                .amount(request.getAmount())
                                .currency(request.getCurrency())
                                .paymentMethod(request.getPaymentMethod())
                                .status(PaymentStatus.PENDING)
                                .description(request.getDescription())
                                .reference(request.getReference())
                                .transactionReference(transactionReference)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                // Don't set version for new entities - R2DBC will handle it
                                .build();

                        // Save payment to database
                        return paymentRepository.save(payment)
                                .doOnSuccess(savedPayment -> {
                                    log.info("Payment saved successfully with ID: {}", savedPayment.getPaymentId());

                                    // Create and send payment created event to Kafka
                                    PaymentCreatedEvent paymentCreatedEvent = PaymentCreatedEvent.builder()
                                            .paymentId(savedPayment.getPaymentId())
                                            .accountId(savedPayment.getAccountId())
                                            .recipientAccountId(savedPayment.getRecipientAccountId())
                                            .amount(savedPayment.getAmount())
                                            .currency(savedPayment.getCurrency())
                                            .paymentMethod(savedPayment.getPaymentMethod().toString())
                                            .status(savedPayment.getStatus().toString())
                                            .description(savedPayment.getDescription())
                                            .reference(savedPayment.getReference())
                                            .transactionReference(savedPayment.getTransactionReference())
                                            .createdAt(savedPayment.getCreatedAt())
                                            .eventType("PAYMENT_CREATED")
                                            .build();

                                    kafkaTemplate.send(paymentCreatedTopic, paymentCreatedEvent);
                                    log.info("Payment created event published to Kafka: {}", savedPayment.getPaymentId());
                                })
                                .map(savedPayment -> PaymentResponse.builder()
                                        .paymentId(savedPayment.getPaymentId())
                                        .accountId(savedPayment.getAccountId())
                                        .recipientAccountId(savedPayment.getRecipientAccountId())
                                        .amount(savedPayment.getAmount())
                                        .currency(savedPayment.getCurrency())
                                        .paymentMethod(savedPayment.getPaymentMethod())
                                        .status(savedPayment.getStatus())
                                        .description(savedPayment.getDescription())
                                        .reference(savedPayment.getReference())
                                        .transactionReference(savedPayment.getTransactionReference())
                                        .gatewayResponse(savedPayment.getGatewayResponse())
                                        .createdAt(savedPayment.getCreatedAt())
                                        .updatedAt(savedPayment.getUpdatedAt())
                                        .message("Payment initiated successfully")
                                        .build())
                                .doOnError(error -> log.error("Error saving payment: {}", error.getMessage()));
                    } else {
                        return Mono.error(new CustomException(INVALID_PARTICIPANT_CODE));
                    }
                });
    }

    @Override
    public Mono<PaymentResponse> getPayment(String paymentId) {
        log.info("Retrieving payment with ID: {}", paymentId);

        return paymentRepository.findById(paymentId)
                .map(payment -> PaymentResponse.builder()
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
                        .message("Payment retrieved successfully")
                        .build())
                .doOnSuccess(paymentResponse -> log.info("Payment retrieved successfully: {}", paymentResponse.getPaymentId()))
                .doOnError(error -> log.error("Error retrieving payment {}: {}", paymentId, error.getMessage()))
                .switchIfEmpty(Mono.error(new CustomException(INVALID_PARTICIPANT_CODE))); // You may want to create a PAYMENT_NOT_FOUND error code
    }
}
