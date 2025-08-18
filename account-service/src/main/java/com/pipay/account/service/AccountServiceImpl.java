package com.pipay.account.service;


import com.pipay.account.constant.AccountStatus;
import com.pipay.account.dto.*;
import com.pipay.account.entity.Account;
import com.pipay.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CURRENCY_KHR = "KHR";
    private static final String CURRENCY_USD = "USD";
    private static final BigDecimal EXCHANGE_RATE = BigDecimal.valueOf(4000);

    private BigDecimal convertToUSD(BigDecimal amount, String currency) {
        if (CURRENCY_KHR.equals(currency)) {
            return amount.divide(EXCHANGE_RATE, 2, RoundingMode.HALF_UP);
        }
        return amount;
    }

    private BigDecimal convertToKHR(BigDecimal amount, String currency) {
        if (CURRENCY_USD.equals(currency)) {
            return amount.multiply(EXCHANGE_RATE);
        }
        return amount;
    }

    @Override
    public AccountResponse getAccountDetails(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> AccountResponse.builder()
                        .accountNumber(account.getAccountNumber())
                        .accountHolderName(account.getAccountHolderName())
                        .balance(account.getBalance())
                        .availableBalance(account.getAvailableBalance())
                        .status(account.getStatus())
                        .type(account.getAccountType())
                        .currency(account.getCurrency())
                        .updatedAt(account.getUpdatedAt())
                        .message("Account retrieved successfully")
                        .build())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found: " + accountNumber));
    }

    @Override
    public List<AccountResponse> getAccountList() {
        return accountRepository.findAll()
                .stream()
                .map(account -> AccountResponse.builder()
                        .accountNumber(account.getAccountNumber())
                        .accountHolderName(account.getAccountHolderName())
                        .balance(account.getBalance())
                        .availableBalance(account.getAvailableBalance())
                        .status(account.getStatus())
                        .type(account.getAccountType())
                        .currency(account.getCurrency())
                        .updatedAt(account.getUpdatedAt())
                        .message("Account retrieved successfully")
                        .build())
                .toList();
    }

    @Override
    public BalanceCheckResponse checkBalance(String accountNumber, BigDecimal amount) {
        log.info("Checking balance for account: {} with amount: {}", accountNumber, amount);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found: " + accountNumber));

        // Check if amount is valid
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid amount for balance check: {}", amount);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }

        // Convert amount to account currency if needed
        BigDecimal convertedAmount = convertToUSD(amount, account.getCurrency());

        // Check if account is active
        if (account.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Account {} is not active. Status: {}", accountNumber, account.getStatus());
            return BalanceCheckResponse.builder()
                    .accountNumber(accountNumber)
                    .currentBalance(account.getBalance())
                    .availableBalance(account.getAvailableBalance())
                    .sufficientFunds(false)
                    .currency(account.getCurrency())
                    .accountStatus(account.getStatus())
                    .build();
        }

        // Check if sufficient funds are available
        boolean sufficientFunds = account.getAvailableBalance().compareTo(convertedAmount) >= 0;

        // Ensure sufficientFunds is correctly calculated and returned
        return BalanceCheckResponse.builder()
                .accountNumber(accountNumber)
                .currentBalance(account.getBalance())
                .availableBalance(account.getAvailableBalance())
                .sufficientFunds(sufficientFunds)
                .currency(account.getCurrency())
                .accountStatus(account.getStatus())
                .build();
    }

    @Override
    @Cacheable(value = "balances", key = "#accountNumber")
    public BalanceResponse getBalance(String accountNumber) {
        log.info("Getting balance for account: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found: " + accountNumber));

        BigDecimal reservedAmount = getReservedAmount(accountNumber);
        BigDecimal availableBalance = account.getBalance().subtract(reservedAmount);

        return BalanceResponse.builder()
                .accountNumber(accountNumber)
                .balance(account.getBalance())
                .availableBalance(availableBalance)
                .currency(account.getCurrency())
                .status(account.getStatus())
                .lastUpdated(account.getUpdatedAt())
                .build();
    }

    @Override
    @CacheEvict(value = {"accounts", "balances"}, key = "#accountNumber")
    public AccountResponse freezeAccount(String accountNumber) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found: " + accountNumber));

        if (account.getStatus().equals(AccountStatus.FROZEN)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is already frozen");
        }

        account.setStatus(AccountStatus.FROZEN);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        // Evict cache
        evictAccountCache(accountNumber);

        return AccountResponse.builder()
                .accountNumber(accountNumber)
                .accountNumber(account.getAccountNumber())
                .status(AccountStatus.FROZEN)
                .type(account.getAccountType())
                .currency(account.getCurrency())
                .updatedAt(account.getUpdatedAt())
                .message("Account frozen successfully")
                .build();
    }

    @Override
    @CacheEvict(value = {"accounts", "balances"}, key = "#accountNumber")
    public AccountResponse unfreezeAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found: " + accountNumber));

        if (account.getStatus().equals(AccountStatus.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is already active");
        }

        account.setStatus(AccountStatus.ACTIVE);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        // Evict cache
        evictAccountCache(accountNumber);

        return AccountResponse.builder()
                .accountNumber(accountNumber)
                .accountNumber(account.getAccountNumber())
                .status(AccountStatus.ACTIVE)
                .type(account.getAccountType())
                .currency(account.getCurrency())
                .updatedAt(account.getUpdatedAt())
                .message("Account unfrozen successfully")
                .build();
    }

    private BigDecimal getReservedAmount(String accountNumber) {
        String key = "reserved:" + accountNumber;
        Object reserved = redisTemplate.opsForValue().get(key);
        return reserved != null ? new BigDecimal(reserved.toString()) : BigDecimal.ZERO;
    }

    public void reserveAmount(String accountNumber, BigDecimal amount) {
        String key = "reserved:" + accountNumber;
        redisTemplate.opsForValue().increment(key, amount.doubleValue());
        redisTemplate.expire(key, Duration.ofMinutes(30));
    }

    public void releaseReservedAmount(String accountNumber, BigDecimal amount) {
        String key = "reserved:" + accountNumber;
        redisTemplate.opsForValue().increment(key, -amount.doubleValue());
    }

    @CacheEvict(value = {"accounts", "balances", "balance-checks"}, key = "#accountNumber")
    public void evictAccountCache(String accountNumber) {
        log.debug("Evicting cache for account: {}", accountNumber);
    }

    @Override
    public AccountValidationResponse validateAccount(String accountNumber) {
        log.info("Validating account: {}", accountNumber);

        try {
            Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElse(null);

            if (account == null) {
                return AccountValidationResponse.builder()
                        .accountNumber(accountNumber)
                        .isValid(false)
                        .message("Account not found")
                        .errorCode("ACCOUNT_NOT_FOUND")
                        .build();
            }

            return AccountValidationResponse.builder()
                    .accountNumber(accountNumber)
                    .isValid(true)
                    .accountStatus(account.getStatus())
                    .accountType(account.getAccountType().toString())
                    .message("Account validation successful")
                    .build();

        } catch (Exception e) {
            log.error("Error validating account {}: {}", accountNumber, e.getMessage());
            return AccountValidationResponse.builder()
                    .accountNumber(accountNumber)
                    .isValid(false)
                    .message("Account validation failed")
                    .errorCode("VALIDATION_ERROR")
                    .build();
        }
    }

    @Override
    @Transactional
    public TransferResponse processTransfer(String sourceAccountNumber, String recipientAccountNumber, BigDecimal amount) {
        log.info("Processing transfer from {} to {} for amount: {}", sourceAccountNumber, recipientAccountNumber, amount);

        String transactionId = UUID.randomUUID().toString();

        try {
            // Validate input parameters
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return createFailureTransferResponse(transactionId, sourceAccountNumber, recipientAccountNumber, amount,
                        "Invalid amount", "INVALID_AMOUNT");
            }

            if (sourceAccountNumber.equals(recipientAccountNumber)) {
                return createFailureTransferResponse(transactionId, sourceAccountNumber, recipientAccountNumber, amount,
                        "Cannot transfer to the same account", "SAME_ACCOUNT_TRANSFER");
            }

            // Get source account
            Account sourceAccount = accountRepository.findByAccountNumber(sourceAccountNumber)
                    .orElse(null);
            if (sourceAccount == null) {
                return createFailureTransferResponse(transactionId, sourceAccountNumber, recipientAccountNumber, amount,
                        "Source account not found", "SOURCE_ACCOUNT_NOT_FOUND");
            }

            // Get recipient account
            Account recipientAccount = accountRepository.findByAccountNumber(recipientAccountNumber)
                    .orElse(null);
            if (recipientAccount == null) {
                return createFailureTransferResponse(transactionId, sourceAccountNumber, recipientAccountNumber, amount,
                        "Recipient account not found", "RECIPIENT_ACCOUNT_NOT_FOUND");
            }

            if (!(sourceAccount.getCurrency().equals(CURRENCY_KHR) || sourceAccount.getCurrency().equals(CURRENCY_USD))) {
                return createFailureTransferResponse(transactionId, sourceAccountNumber, recipientAccountNumber, amount,
                        "Unsupported source account currency: " + sourceAccount.getCurrency(), "UNSUPPORTED_CURRENCY");
            }

            if (!(recipientAccount.getCurrency().equals(CURRENCY_KHR) || recipientAccount.getCurrency().equals(CURRENCY_USD))) {
                return createFailureTransferResponse(transactionId, sourceAccountNumber, recipientAccountNumber, amount,
                        "Unsupported source account currency: " + sourceAccount.getCurrency(), "UNSUPPORTED_CURRENCY");
            }

            // Check account statuses
            if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
                return createFailureTransferResponse(transactionId, sourceAccountNumber, recipientAccountNumber, amount,
                        "Source account is " + sourceAccount.getStatus().getDescription(), "SOURCE_ACCOUNT_INACTIVE");
            }

            if (recipientAccount.getStatus() != AccountStatus.ACTIVE) {
                return createFailureTransferResponse(transactionId, sourceAccountNumber, recipientAccountNumber, amount,
                        "Recipient account is " + recipientAccount.getStatus().getDescription(), "RECIPIENT_ACCOUNT_INACTIVE");
            }

            // Check sufficient funds in source account
            BigDecimal convertedAmount = convertToUSD(amount, sourceAccount.getCurrency());
            if (sourceAccount.getAvailableBalance().compareTo(convertedAmount) < 0) {
                return createFailureTransferResponse(transactionId, sourceAccountNumber, recipientAccountNumber, amount,
                        "Insufficient funds", "INSUFFICIENT_FUNDS");
            }

            // Validate recipient account currency
            if (!CURRENCY_KHR.equals(recipientAccount.getCurrency()) && !CURRENCY_USD.equals(recipientAccount.getCurrency())) {
                return createFailureTransferResponse(transactionId, sourceAccountNumber, recipientAccountNumber, amount,
                        "Unsupported recipient account currency: " + recipientAccount.getCurrency(), "UNSUPPORTED_CURRENCY");
            }

            // Convert amount based on source and recipient currencies
            BigDecimal sourceDeductedAmount;
            BigDecimal recipientAddedAmount;

            if (CURRENCY_USD.equals(sourceAccount.getCurrency()) && CURRENCY_KHR.equals(recipientAccount.getCurrency())) {
                // Convert USD to KHR
                sourceDeductedAmount = amount;
                recipientAddedAmount = convertToKHR(amount, sourceAccount.getCurrency());
            } else if (CURRENCY_KHR.equals(sourceAccount.getCurrency()) && CURRENCY_USD.equals(recipientAccount.getCurrency())) {
                // Convert KHR to USD
                sourceDeductedAmount = amount;
                recipientAddedAmount = convertToUSD(amount, sourceAccount.getCurrency());
            } else {
                // Same currency, no conversion needed
                sourceDeductedAmount = amount;
                recipientAddedAmount = amount;
            }

            // Deduct from source account
            sourceAccount.setBalance(sourceAccount.getBalance().subtract(sourceDeductedAmount));
            sourceAccount.setAvailableBalance(sourceAccount.getAvailableBalance().subtract(sourceDeductedAmount));
            sourceAccount.setUpdatedAt(LocalDateTime.now());

            // Add to recipient account
            recipientAccount.setBalance(recipientAccount.getBalance().add(recipientAddedAmount));
            recipientAccount.setAvailableBalance(recipientAccount.getAvailableBalance().add(recipientAddedAmount));
            recipientAccount.setUpdatedAt(LocalDateTime.now());

            // Save both accounts
            accountRepository.save(sourceAccount);
            accountRepository.save(recipientAccount);

            // Evict caches for both accounts
            evictAccountCache(sourceAccountNumber);
            evictAccountCache(recipientAccountNumber);

            return TransferResponse.builder()
                    .transactionId(transactionId)
                    .sourceAccountNumber(sourceAccountNumber)
                    .recipientAccountNumber(recipientAccountNumber)
                    .amount(recipientAddedAmount)
                    .currency(recipientAccount.getCurrency())
                    .success(true)
                    .status("COMPLETED")
                    .message("Transfer completed successfully")
                    .processedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error processing transfer from {} to {}: {}", sourceAccountNumber, recipientAccountNumber, e.getMessage());
            return createFailureTransferResponse(transactionId, sourceAccountNumber, recipientAccountNumber, amount,
                    "Transfer processing failed: " + e.getMessage(), "PROCESSING_ERROR");
        }
    }

    private TransferResponse createFailureTransferResponse(String transactionId, String sourceAccountNumber,
                                                           String recipientAccountNumber, BigDecimal amount,
                                                           String message, String errorCode) {
        return TransferResponse.builder()
                .transactionId(transactionId)
                .sourceAccountNumber(sourceAccountNumber)
                .recipientAccountNumber(recipientAccountNumber)
                .amount(amount)
                .success(false)
                .status("FAILED")
                .message(message)
                .errorCode(errorCode)
                .processedAt(LocalDateTime.now())
                .build();
    }
}
