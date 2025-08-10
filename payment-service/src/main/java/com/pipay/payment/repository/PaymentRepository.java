package com.pipay.payment.repository;

import com.pipay.payment.entity.Payment;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PaymentRepository extends R2dbcRepository<Payment, String> {

    Flux<Payment> findByAccountNumber(String accountNumber);
    Flux<Payment> findByRecipientAccountNumber(String recipientAccountNumber);
    Mono<Payment> findByTransactionReference(String transactionReference);

}
