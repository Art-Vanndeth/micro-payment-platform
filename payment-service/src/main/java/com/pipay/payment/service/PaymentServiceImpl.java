package com.pipay.payment.service;

import com.pipay.payment.dto.PaymentRequest;
import com.pipay.payment.dto.PaymentResponse;
import com.pipay.payment.exception.CustomException;
import com.pipay.payment.integration.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.pipay.payment.constant.error.ErrorCode.INVALID_PARTICIPANT_CODE;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${kafka.topics.payment-events}")
    private String paymentTopic;

    private final AccountService accountService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public Mono<PaymentResponse> payment(PaymentRequest request) {
        return accountService.checkBalance(request.getAccountId(), request.getAmount())
                .flatMap(response -> {
                    log.info("Checking balance for account: {}", response);
                    if (response.isSufficientFunds()) {
                        PaymentResponse paymentResponse = PaymentResponse.builder()
                                .build();

                        PaymentRequest paymentRequest = PaymentRequest.builder()
                                .accountId(request.getAccountId())
                                .amount(request.getAmount())
                                .currency(request.getCurrency())
                                .paymentMethod(request.getPaymentMethod())
                                .recipientAccountId(request.getRecipientAccountId())
                                .description(request.getDescription())
                                .reference(request.getReference())
                                .cardToken(request.getCardToken())
                                .paymentGateway(request.getPaymentGateway())
                                .build();
                        kafkaTemplate.send(paymentTopic, paymentRequest.toString());


                        return Mono.just(paymentResponse);
                    } else {
                        return Mono.error(new CustomException(INVALID_PARTICIPANT_CODE));
                    }
                });
    }

    @Override
    public Mono<PaymentResponse> getPayment(String paymentId) {
        return null;
    }
}
