package com.pipay.transaction.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipay.transaction.consumer.PaymentEventConsumer;
import com.pipay.transaction.service.TransactionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public PaymentEventConsumer paymentEventConsumer(TransactionService transactionService, ObjectMapper objectMapper) {
        return new PaymentEventConsumer(transactionService, objectMapper);
    }
}
