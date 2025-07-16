package com.pipay.payment.service;

import com.pipay.payment.common.BaseResult;
import com.pipay.payment.dto.PaymentRequest;
import com.pipay.payment.dto.PaymentResponse;
import reactor.core.publisher.Mono;

public interface PaymentService {

    Mono<PaymentResponse> payment(PaymentRequest request);

    Mono<PaymentResponse> getPayment(String paymentId);

}
