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
    private final CorePaymentService corePaymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Mono<PaymentResponse> payment(PaymentRequest request) {
        log.info("Processing payment request from {} to {} for amount: {}",
                request.getAccountId(), request.getRecipientAccountId(), request.getAmount());

        // First validate both source and recipient accounts
        return corePaymentService.validatePaymentAccounts(
                request.getAccountId(),
                request.getRecipientAccountId(),
                request.getAmount())
                .then(Mono.defer(() -> {
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
                            .build();

                    // Save payment to database first
                    return paymentRepository.save(payment)
                            .flatMap(savedPayment -> {
                                log.info("Payment saved with ID: {}, now processing account deduction", savedPayment.getPaymentId());

                                // Process account deduction through core service
                                return corePaymentService.processAccountDeduction(
                                        savedPayment.getAccountId(),
                                        savedPayment.getRecipientAccountId(),
                                        savedPayment.getAmount())
                                        .flatMap(deductionSuccess -> {
                                            if (deductionSuccess) {
                                                // Update payment status to COMPLETED
                                                savedPayment.setStatus(PaymentStatus.COMPLETED);
                                                savedPayment.setUpdatedAt(LocalDateTime.now());

                                                return paymentRepository.save(savedPayment)
                                                        .doOnSuccess(completedPayment -> {
                                                            log.info("Payment completed successfully with ID: {}", completedPayment.getPaymentId());

                                                            // Create and send payment created event to Kafka
                                                            PaymentCreatedEvent paymentCreatedEvent = PaymentCreatedEvent.builder()
                                                                    .paymentId(completedPayment.getPaymentId())
                                                                    .accountId(completedPayment.getAccountId())
                                                                    .recipientAccountId(completedPayment.getRecipientAccountId())
                                                                    .amount(completedPayment.getAmount())
                                                                    .currency(completedPayment.getCurrency())
                                                                    .paymentMethod(completedPayment.getPaymentMethod().toString())
                                                                    .status(completedPayment.getStatus().toString())
                                                                    .description(completedPayment.getDescription())
                                                                    .reference(completedPayment.getReference())
                                                                    .transactionReference(completedPayment.getTransactionReference())
                                                                    .createdAt(completedPayment.getCreatedAt())
                                                                    .eventType("PAYMENT_COMPLETED")
                                                                    .build();

                                                            kafkaTemplate.send(paymentCreatedTopic, paymentCreatedEvent);
                                                            log.info("Payment completed event published to Kafka: {}", completedPayment.getPaymentId());
                                                        });
                                            } else {
                                                // Update payment status to FAILED
                                                savedPayment.setStatus(PaymentStatus.FAILED);
                                                savedPayment.setUpdatedAt(LocalDateTime.now());

                                                return paymentRepository.save(savedPayment)
                                                        .then(Mono.error(new CustomException(INVALID_PARTICIPANT_CODE)));
                                            }
                                        });
                            })
                            .map(finalPayment -> PaymentResponse.builder()
                                    .paymentId(finalPayment.getPaymentId())
                                    .accountId(finalPayment.getAccountId())
                                    .recipientAccountId(finalPayment.getRecipientAccountId())
                                    .amount(finalPayment.getAmount())
                                    .currency(finalPayment.getCurrency())
                                    .paymentMethod(finalPayment.getPaymentMethod())
                                    .status(finalPayment.getStatus())
                                    .description(finalPayment.getDescription())
                                    .reference(finalPayment.getReference())
                                    .transactionReference(finalPayment.getTransactionReference())
                                    .gatewayResponse(finalPayment.getGatewayResponse())
                                    .createdAt(finalPayment.getCreatedAt())
                                    .updatedAt(finalPayment.getUpdatedAt())
                                    .message("Payment processed successfully")
                                    .build())
                            .doOnError(error -> log.error("Error processing payment: {}", error.getMessage()));
                }));
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
