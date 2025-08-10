package com.pipay.payment.service;

import com.pipay.payment.constant.PaymentStatus;
import com.pipay.payment.dto.PaymentRequest;
import com.pipay.payment.dto.PaymentResponse;
import com.pipay.payment.entity.Payment;
import com.pipay.payment.event.PaymentCreatedEvent;
import com.pipay.payment.exception.CustomException;
import com.pipay.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.pipay.payment.constant.error.ErrorCode.INVALID_PARTICIPANT_CODE;
import static com.pipay.payment.constant.error.ErrorCode.UNSUPPORTED_CURRENCY;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${kafka.topics.payment-created-events-topic}")
    private String paymentCreatedTopic;

    private final PaymentRepository paymentRepository;
    private final CorePaymentService corePaymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Mono<PaymentResponse> payment(PaymentRequest request) {
        log.info("Processing payment request: {}", request);

        if (!("KHR".equals(request.getCurrency()) || "USD".equals(request.getCurrency()))) {
            log.error("Unsupported currency: {}", request.getCurrency());
            return Mono.error(new CustomException(UNSUPPORTED_CURRENCY));
        }

        return corePaymentService.validatePaymentAccounts(
                        request.getAccountNumber(),
                        request.getRecipientAccountNumber(),
                        request.getAmount())
                .then(createAndSavePayment(request))
                .flatMap(this::processPayment)
                .map(this::buildPaymentResponse)
                .doOnError(error -> log.error("Error processing payment: {}", error.getMessage()));
    }

    private Mono<Payment> createAndSavePayment(PaymentRequest request) {
        String paymentId = UUID.randomUUID().toString();
        String transactionReference = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .accountNumber(request.getAccountNumber())
                .recipientAccountNumber(request.getRecipientAccountNumber())
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

        return paymentRepository.save(payment)
                .doOnSuccess(savedPayment -> log.info("Payment saved with ID: {}", savedPayment.getPaymentId()));
    }

    private Mono<Payment> processPayment(Payment payment) {
        return corePaymentService.processAccountDeduction(
                        payment.getAccountNumber(),
                        payment.getRecipientAccountNumber(),
                        payment.getAmount())
                .flatMap(deductionSuccess -> {
                    if (deductionSuccess) {
                        payment.setStatus(PaymentStatus.COMPLETED);
                        payment.setUpdatedAt(LocalDateTime.now());
                        return paymentRepository.save(payment)
                                .doOnSuccess(this::publishPaymentEvent);
                    } else {
                        payment.setStatus(PaymentStatus.FAILED);
                        payment.setUpdatedAt(LocalDateTime.now());
                        return paymentRepository.save(payment)
                                .then(Mono.error(new CustomException(INVALID_PARTICIPANT_CODE)));
                    }
                });
    }

    private void publishPaymentEvent(Payment payment) {
        PaymentCreatedEvent paymentCreatedEvent = PaymentCreatedEvent.builder()
                .paymentId(payment.getPaymentId())
                .accountNumber(payment.getAccountNumber())
                .recipientAccountNumber(payment.getRecipientAccountNumber())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod().toString())
                .status(payment.getStatus().toString())
                .description(payment.getDescription())
                .reference(payment.getReference())
                .transactionReference(payment.getTransactionReference())
                .createdAt(payment.getCreatedAt())
                .eventType("PAYMENT_COMPLETED")
                .build();

        kafkaTemplate.send(paymentCreatedTopic, paymentCreatedEvent);
        log.info("Payment event published to Kafka: {}", payment.getPaymentId());
    }

    private PaymentResponse buildPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .accountNumber(payment.getAccountNumber())
                .recipientAccountNumber(payment.getRecipientAccountNumber())
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
                .message("Payment processed successfully")
                .build();
    }

    @Override
    public Mono<PaymentResponse> getPayment(String paymentId) {
        log.info("Retrieving payment with ID: {}", paymentId);

        return paymentRepository.findById(paymentId)
                .map(payment -> PaymentResponse.builder()
                        .paymentId(payment.getPaymentId())
                        .accountNumber(payment.getAccountNumber())
                        .recipientAccountNumber(payment.getRecipientAccountNumber())
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
