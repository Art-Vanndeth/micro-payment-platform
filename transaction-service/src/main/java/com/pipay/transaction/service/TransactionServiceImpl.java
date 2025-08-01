package com.pipay.transaction.service;

import com.pipay.transaction.constant.TransactionStatus;
import com.pipay.transaction.constant.TransactionType;
import com.pipay.transaction.dto.CreateTransactionRequest;
import com.pipay.transaction.entity.Transaction;
import com.pipay.transaction.event.PaymentEvent;
import com.pipay.transaction.event.TransactionEvent;
import com.pipay.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    @Value("${kafka.topics.transaction-events}")
    private String transactionEventTopic;

    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Mono<Transaction> createTransaction(CreateTransactionRequest request) {
        return Mono.fromCallable(() -> {
            log.info("Creating transaction for payment: {}", request.getPaymentId());

            // Generate unique transaction ID
            String transactionId = generateTransactionId();

            // Build transaction entity
            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .paymentId(request.getPaymentId())
                    .fromAccountId(request.getFromAccountId())
                    .toAccountId(request.getToAccountId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency().toUpperCase())
                    .transactionType(request.getTransactionType())
                    .status(TransactionStatus.INITIATED)
                    .description(request.getDescription())
                    .gatewayTransactionId(request.getGatewayTransactionId())
                    .createdBy("system")
                    .metadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>())
                    .build();

            return transaction;
        })
                .flatMap(this::validateTransaction)
                .flatMap(this::saveTransaction)
                .doOnSuccess(transaction ->
                        log.info("Transaction created successfully: {}", transaction.getTransactionId()))
                .doOnError(error ->
                        log.error("Failed to create transaction: {}", error.getMessage()));
    }

    @Override
    public Mono<Transaction> createTransactionFromPayment(PaymentEvent paymentEvent) {
        log.info("Creating transaction from payment event: {}", paymentEvent.getPaymentId());

        // Create debit transaction for sender
        Transaction debitTransaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .paymentId(paymentEvent.getPaymentId())
                .fromAccountId(paymentEvent.getAccountId())
                .toAccountId(paymentEvent.getRecipientAccountId())
                .amount(paymentEvent.getAmount())
                .currency(paymentEvent.getCurrency())
                .transactionType(TransactionType.DEBIT)
                .status(TransactionStatus.INITIATED)
                .description(paymentEvent.getDescription())
                .createdBy("payment-service")
                .build();

        return transactionRepository.save(debitTransaction)
                .doOnSuccess(savedTransaction -> {
                    log.info("Debit transaction created successfully: {}", savedTransaction.getTransactionId());
                    publishTransactionEvent(savedTransaction, "TRANSACTION_CREATED");
                })
                .flatMap(debitTx -> {
                    // Create credit transaction for recipient
                    Transaction creditTransaction = Transaction.builder()
                            .transactionId(UUID.randomUUID().toString())
                            .paymentId(paymentEvent.getPaymentId())
                            .fromAccountId(paymentEvent.getAccountId())
                            .toAccountId(paymentEvent.getRecipientAccountId())
                            .amount(paymentEvent.getAmount())
                            .currency(paymentEvent.getCurrency())
                            .transactionType(TransactionType.CREDIT)
                            .status(TransactionStatus.INITIATED)
                            .description(paymentEvent.getDescription())
                            .createdBy("payment-service")
                            .build();

                    return transactionRepository.save(creditTransaction)
                            .doOnSuccess(savedCreditTransaction -> {
                                log.info("Credit transaction created successfully: {}", savedCreditTransaction.getTransactionId());
                                publishTransactionEvent(savedCreditTransaction, "TRANSACTION_CREATED");
                            })
                            .map(creditTx -> debitTx); // Return the debit transaction as primary
                })
                .doOnError(error -> log.error("Error creating transaction from payment: {}", error.getMessage()));
    }

    @Override
    public Mono<Transaction> updateTransactionStatus(String transactionId,
                                                   TransactionStatus status,
                                                   String gatewayResponse) {
        log.info("Updating transaction status: {} to {}", transactionId, status);

        return transactionRepository.findById(transactionId)
                .switchIfEmpty(Mono.error(new RuntimeException("Transaction not found: " + transactionId)))
                .flatMap(transaction -> {
                    transaction.setStatus(status);
                    transaction.setGatewayResponse(gatewayResponse);

                    if (status == TransactionStatus.COMPLETED) {
                        transaction.setCompletedAt(LocalDateTime.now());
                    } else if (status == TransactionStatus.FAILED) {
                        transaction.setFailureReason(gatewayResponse);
                    }

                    return transactionRepository.save(transaction);
                })
                .doOnSuccess(updatedTransaction -> {
                    log.info("Transaction status updated successfully: {}", transactionId);
                    publishTransactionEvent(updatedTransaction, "TRANSACTION_STATUS_UPDATED");
                })
                .doOnError(error -> log.error("Error updating transaction status {}: {}", transactionId, error.getMessage()));
    }

    @Override
    public Mono<Transaction> getTransactionById(String transactionId) {
        log.info("Retrieving transaction by ID: {}", transactionId);
        return transactionRepository.findById(transactionId)
                .switchIfEmpty(Mono.error(new RuntimeException("Transaction not found: " + transactionId)))
                .doOnSuccess(transaction -> log.info("Transaction found: {}", transactionId))
                .doOnError(error -> log.error("Error retrieving transaction {}: {}", transactionId, error.getMessage()));
    }

    @Override
    public Flux<Transaction> getTransactionsByPaymentId(String paymentId) {
        log.info("Retrieving transactions by payment ID: {}", paymentId);
        return transactionRepository.findByPaymentId(paymentId)
                .doOnComplete(() -> log.info("Retrieved transactions for payment: {}", paymentId))
                .doOnError(error -> log.error("Error retrieving transactions for payment {}: {}", paymentId, error.getMessage()));
    }

    @Override
    public Flux<Transaction> getTransactionsByAccountId(String accountId) {
        log.info("Retrieving transactions by account ID: {}", accountId);
        return transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId)
                .doOnComplete(() -> log.info("Retrieved transactions for account: {}", accountId))
                .doOnError(error -> log.error("Error retrieving transactions for account {}: {}", accountId, error.getMessage()));
    }

    @Override
    public Flux<Transaction> getTransactionsByStatus(TransactionStatus status) {
        log.info("Retrieving transactions by status: {}", status);
        return transactionRepository.findByStatus(status)
                .doOnComplete(() -> log.info("Retrieved transactions for status: {}", status))
                .doOnError(error -> log.error("Error retrieving transactions for status {}: {}", status, error.getMessage()));
    }

    @Override
    public Mono<Transaction> processTransaction(String transactionId) {
        return getTransactionById(transactionId)
                .flatMap(transaction -> {
                    if (transaction.getStatus() != TransactionStatus.INITIATED) {
                        return Mono.error(new RuntimeException("Transaction cannot be processed in current status: " + transaction.getStatus()));
                    }

                    // Update status to processing
                    transaction.setStatus(TransactionStatus.PROCESSING);
                    return transactionRepository.save(transaction);
                })
                .flatMap(this::callPaymentGateway)
                .doOnSuccess(transaction ->
                        log.info("Transaction processed: {}", transaction.getTransactionId()));
    }

    // Private helper methods
    private Mono<Transaction> validateTransaction(Transaction transaction) {
        return Mono.fromCallable(() -> {
            if (transaction.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Transaction amount must be positive");
            }

            if (transaction.getFromAccountId().equals(transaction.getToAccountId())) {
                throw new IllegalArgumentException("From and To accounts cannot be the same");
            }

            return transaction;
        });
    }

    private Mono<Transaction> saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction)
                .doOnSuccess(saved -> log.debug("Transaction saved to database: {}", saved.getTransactionId()))
                .onErrorMap(ex -> new RuntimeException("Failed to save transaction", ex));
    }

    private Mono<Transaction> callPaymentGateway(Transaction transaction) {
        return Mono.delay(Duration.ofMillis(100))  // Non-blocking delay instead of Thread.sleep
                .then(Mono.fromCallable(() -> {
                    // Simulate gateway response (90% success rate)
                    boolean success = Math.random() > 0.1;

                    if (success) {
                        transaction.setStatus(TransactionStatus.COMPLETED);
                        transaction.setCompletedAt(LocalDateTime.now());
                        transaction.setGatewayResponse("SUCCESS");
                        transaction.setGatewayTransactionId("GTW-" + UUID.randomUUID().toString().substring(0, 8));
                    } else {
                        transaction.setStatus(TransactionStatus.FAILED);
                        transaction.setFailureReason("Gateway declined transaction");
                        transaction.setGatewayResponse("DECLINED");
                    }

                    return transaction;
                }))
                .flatMap(transactionRepository::save);
    }

    private void publishTransactionEvent(Transaction transaction, String eventType) {
        TransactionEvent transactionEvent = TransactionEvent.builder()
                .transactionId(transaction.getTransactionId())
                .paymentId(transaction.getPaymentId())
                .fromAccountId(transaction.getFromAccountId())
                .toAccountId(transaction.getToAccountId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .transactionType(transaction.getTransactionType().toString())
                .status(transaction.getStatus().toString())
                .description(transaction.getDescription())
                .reference(transaction.getReference())
                .transactionReference(transaction.getTransactionReference())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .eventType(eventType)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(transactionEventTopic, transactionEvent);
        log.info("Transaction event published to Kafka: {} - {}", transaction.getTransactionId(), eventType);
    }

    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
