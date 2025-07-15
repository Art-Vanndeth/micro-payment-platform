package com.pipay.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayResponse {
    private String transactionId;
    private String transactionReference;
    private String responseMessage;
    private String responseCode;
    private boolean success;
    private String gatewayName;
}
