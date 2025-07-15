package com.pipay.payment.controller;

import com.pipay.payment.dto.PaymentRequest;
import com.pipay.payment.dto.PaymentResponse;
import com.pipay.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Action 1: Receive Payment Request - API endpoint receives payment
     */
    @PostMapping("/process")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Received payment request for account: {} amount: {} {}",
                request.getAccountId(), request.getAmount(), request.getCurrency());

        return paymentService.processPayment(request)
                .doOnSuccess(response -> log.info("Payment processing completed: {}", response.getPaymentId()))
                .doOnError(error -> log.error("Payment processing failed: {}", error.getMessage()));
    }

    @GetMapping("/{paymentId}")
    public Mono<PaymentResponse> getPayment(@PathVariable String paymentId) {
        log.debug("Retrieving payment: {}", paymentId);
        return paymentService.getPayment(paymentId);
    }
}
