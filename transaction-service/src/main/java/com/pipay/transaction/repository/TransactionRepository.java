package com.pipay.transaction.repository;

import com.pipay.transaction.entity.Transaction;
import com.pipay.transaction.constant.TransactionStatus;
import com.pipay.transaction.constant.TransactionType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {

    // Find transactions by payment ID
    Flux<Transaction> findByPaymentId(String paymentId);

    // Find transactions by account (either from or to)
    Flux<Transaction> findByFromAccountIdOrToAccountId(String fromAccountId, String toAccountId);

    // Find transactions by status
    Flux<Transaction> findByStatus(TransactionStatus status);

    // Find transactions by type
    Flux<Transaction> findByTransactionType(TransactionType transactionType);

    // Find transactions by account and status
    Flux<Transaction> findByFromAccountIdAndStatus(String accountId, TransactionStatus status);

    // Find transactions within date range
    Flux<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Custom query to find transactions by account and date range
    @Query("{ $and: [ { $or: [ { 'fromAccountId': ?0 }, { 'toAccountId': ?0 } ] }, { 'createdAt': { $gte: ?1, $lte: ?2 } } ] }")
    Flux<Transaction> findByAccountIdAndDateRange(String accountId, LocalDateTime startDate, LocalDateTime endDate);

    // Count transactions by status
    Mono<Long> countByStatus(TransactionStatus status);
}
