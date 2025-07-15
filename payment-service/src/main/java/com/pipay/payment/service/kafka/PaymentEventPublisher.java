package com.pipay.payment.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipay.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventPublisher {

    @Qualifier("paymentReactiveKafkaProducerTemplate")
    private final ReactiveKafkaProducerTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    private static final String PAYMENT_TOPIC = "payment-events";

    public Mono<Void> publishPaymentEvent(Payment payment) {
        return Mono.fromCallable(() -> {
            try {
                return objectMapper.writeValueAsString(payment);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize payment event", e);
            }
        })
        .flatMap(paymentJson ->
            kafkaTemplate.send(PAYMENT_TOPIC, payment.getPaymentId(), paymentJson)
                    .doOnSuccess(result -> log.debug("Payment event published: {}", payment.getPaymentId()))
                    .doOnError(error -> log.error("Failed to publish payment event: {}", error.getMessage()))
                    .then()
        );
    }
}
