package com.pipay.transaction.service;

import com.pipay.transaction.constant.TransactionStatus;
import com.pipay.transaction.dto.CreateTransactionRequest;
import com.pipay.transaction.entity.Transaction;
import com.pipay.transaction.event.PaymentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {

    /**
     * Creates a new transaction with proper validation and business logic
     */
    Mono<Transaction> createTransaction(CreateTransactionRequest request);

    /**
     * Creates transactions from payment event (both debit and credit)
     */
    Mono<Transaction> createTransactionFromPayment(PaymentEvent paymentEvent);

    /**
     * Updates transaction status
     */
    Mono<Transaction> updateTransactionStatus(String transactionId,
                                            TransactionStatus status,
                                            String gatewayResponse);

    /**
     * Retrieves transaction by ID
     */
    Mono<Transaction> getTransactionById(String transactionId);

    /**
     * Retrieves transactions by payment ID
     */
    Flux<Transaction> getTransactionsByPaymentId(String paymentId);

    /**
     * Retrieves transactions by account ID (both from and to)
     */
    Flux<Transaction> getTransactionsByAccountId(String accountId);

    /**
     * Retrieves transactions by status
     */
    Flux<Transaction> getTransactionsByStatus(TransactionStatus status);

    /**
     * Process transaction - involves external payment gateway calls
     */
    Mono<Transaction> processTransaction(String transactionId);
}
