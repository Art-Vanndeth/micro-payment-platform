package com.pipay.transaction.controller;

import com.pipay.transaction.constant.TransactionStatus;
import com.pipay.transaction.dto.CreateTransactionRequest;
import com.pipay.transaction.entity.Transaction;
import com.pipay.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Transaction> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        log.info("Received create transaction request for payment: {}", request.getPaymentId());
        return transactionService.createTransaction(request);
    }

    @GetMapping("/{transactionId}")
    public Mono<Transaction> getTransaction(@PathVariable String transactionId) {
        return transactionService.getTransactionById(transactionId);
    }

    @GetMapping("/payment/{paymentId}")
    public Flux<Transaction> getTransactionsByPaymentId(@PathVariable String paymentId) {
        return transactionService.getTransactionsByPaymentId(paymentId);
    }

    @GetMapping("/account/{accountNumber}")
    public Flux<Transaction> getTransactionsByAccountNumber(@PathVariable String accountNumber) {
        return transactionService.getTransactionsByAccountNumber(accountNumber);
    }

    @GetMapping("/status/{status}")
    public Flux<Transaction> getTransactionsByStatus(@PathVariable TransactionStatus status) {
        return transactionService.getTransactionsByStatus(status);
    }

    @PutMapping("/{transactionId}/status")
    public Mono<Transaction> updateTransactionStatus(
            @PathVariable String transactionId,
            @RequestParam TransactionStatus status,
            @RequestParam(required = false) String gatewayResponse) {
        return transactionService.updateTransactionStatus(transactionId, status, gatewayResponse);
    }

    @PostMapping("/{transactionId}/process")
    public Mono<Transaction> processTransaction(@PathVariable String transactionId) {
        return transactionService.processTransaction(transactionId);
    }
}
