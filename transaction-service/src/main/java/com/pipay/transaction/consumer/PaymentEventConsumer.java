package com.pipay.transaction.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipay.transaction.event.PaymentEvent;
import com.pipay.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;

@Slf4j
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.payment-created-events-topic}", groupId = "${kafka.consumer.group-id}")
    public void handlePaymentCreatedEvent(@Payload String message) {
        try {
            log.info("Received payment created event: {}", message);

            // Parse the payment event
            PaymentEvent paymentEvent = objectMapper.readValue(message, PaymentEvent.class);
            log.info("Parsed payment event for payment ID: {}", paymentEvent.getPaymentId());

            // Create transaction from payment event
            transactionService.createTransactionFromPayment(paymentEvent)
                    .subscribe(
                            transaction -> log.info("Successfully created transaction from payment: {}", transaction.getTransactionId()),
                            error -> log.error("Error creating transaction from payment {}: {}", paymentEvent.getPaymentId(), error.getMessage())
                    );

        } catch (Exception e) {
            log.error("Error processing payment created event: {}", e.getMessage(), e);
        }
    }
}
