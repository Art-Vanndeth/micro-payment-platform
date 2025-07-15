package com.pipay.payment.service;

import com.pipay.payment.dto.PaymentRequest;
import com.pipay.payment.dto.PaymentResponse;
import reactor.core.publisher.Mono;

public interface PaymentService {

    /**
     * Process payment following all 12 actions
     * 1. Receive Payment Request
     * 2. Validate Request Data
     * 3. Generate Payment ID
     * 4. Check Cache
     * 5. Call Account Service
     * 6. Validate Business Rules
     * 7. Call Payment Gateway
     * 8. Process Gateway Response
     * 9. Update Payment Status
     * 10. Publish Payment Event
     * 11. Cache Payment Result
     * 12. Return Payment Response
     */
    Mono<PaymentResponse> processPayment(PaymentRequest request);

    Mono<PaymentResponse> getPayment(String paymentId);
}
