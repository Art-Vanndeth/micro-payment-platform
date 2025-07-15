package com.pipay.account.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipay.account.constant.AccountStatus;
import com.pipay.account.constant.AccountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "accounts")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String accountId;

    @NotNull
    @NotBlank
    @Column(unique = true)
    private String accountNumber;

    @NotNull
    @NotBlank
    private String userId;

    @NotNull
    @NotBlank
    private String accountHolderName;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @NotNull
    @DecimalMin(value = "0.00")
    @Column(precision = 19, scale = 2)
    private BigDecimal balance;

    @NotNull
    @DecimalMin(value = "0.00")
    @Column(precision = 19, scale = 2)
    private BigDecimal availableBalance;

    @NotNull
    @Size(min = 3, max = 3)
    private String currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
