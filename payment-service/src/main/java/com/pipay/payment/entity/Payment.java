package com.pipay.payment.entity;

import com.pipay.payment.constant.PaymentMethod;
import com.pipay.payment.constant.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table("payments")
public class Payment implements Persistable<String> {

    @Id
    private String paymentId;
    private String accountId;
    private String recipientAccountId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String description;
    private String reference;
    private String transactionReference;
    private String gatewayResponse;
    private String gatewayTransactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    @Transient
    private boolean isNew = true;

    @Override
    public String getId() {
        return this.paymentId;
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    public void markNotNew() {
        this.isNew = false;
    }

    // Custom builder method to ensure isNew is set correctly
    public static PaymentBuilder builder() {
        return new PaymentBuilder() {
            @Override
            public Payment build() {
                Payment payment = super.build();
                payment.isNew = true; // Ensure new payments are marked as new
                return payment;
            }
        };
    }

    // Custom toBuilder method to preserve isNew state
    public PaymentBuilder toBuilder() {
        PaymentBuilder builder = new PaymentBuilder()
                .paymentId(this.paymentId)
                .accountId(this.accountId)
                .recipientAccountId(this.recipientAccountId)
                .amount(this.amount)
                .currency(this.currency)
                .paymentMethod(this.paymentMethod)
                .status(this.status)
                .description(this.description)
                .reference(this.reference)
                .transactionReference(this.transactionReference)
                .gatewayResponse(this.gatewayResponse)
                .gatewayTransactionId(this.gatewayTransactionId)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .version(this.version);

        return new PaymentBuilder() {
            @Override
            public Payment build() {
                Payment newPayment = builder.build();
                newPayment.isNew = false; // Existing payments should not be marked as new
                return newPayment;
            }
        };
    }
}
