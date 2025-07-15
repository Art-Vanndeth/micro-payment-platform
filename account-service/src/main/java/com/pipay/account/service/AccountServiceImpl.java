package com.pipay.account.service;


import com.pipay.account.constant.AccountStatus;
import com.pipay.account.dto.AccountResponse;
import com.pipay.account.dto.BalanceCheckResponse;
import com.pipay.account.dto.BalanceResponse;
import com.pipay.account.entity.Account;
import com.pipay.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Cacheable(value = "balance-checks", key = "#accountId")
    public BalanceCheckResponse checkBalance(String accountId, BigDecimal amount) {
        log.info("Checking balance for account: {} with amount: {}", accountId, amount);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found: " + accountId));

        // Check if account is active
        if (account.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Account {} is not active. Status: {}", accountId, account.getStatus());
            return BalanceCheckResponse.builder()
                    .accountId(accountId)
                    .currentBalance(account.getBalance())
                    .availableBalance(account.getAvailableBalance())
                    .sufficientFunds(false)
                    .currency(account.getCurrency())
                    .accountStatus(account.getStatus())
                    .build();
        }

        // Check if amount is valid
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid amount for balance check: {}", amount);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }

        // Check if sufficient funds are available
        boolean sufficientFunds = account.getAvailableBalance().compareTo(amount) >= 0;

        return BalanceCheckResponse.builder()
                .accountId(accountId)
                .currentBalance(account.getBalance())
                .availableBalance(account.getAvailableBalance())
                .sufficientFunds(sufficientFunds)
                .currency(account.getCurrency())
                .accountStatus(account.getStatus())
                .build();
    }

    @Override
    @Cacheable(value = "balances", key = "#accountId")
    public BalanceResponse getBalance(String accountId) {
        log.info("Getting balance for account: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found: " + accountId));

        BigDecimal reservedAmount = getReservedAmount(accountId);
        BigDecimal availableBalance = account.getBalance().subtract(reservedAmount);

        return BalanceResponse.builder()
                .accountId(accountId)
                .balance(account.getBalance())
                .availableBalance(availableBalance)
                .currency(account.getCurrency())
                .status(account.getStatus())
                .lastUpdated(account.getUpdatedAt())
                .build();
    }

    @Override
    @CacheEvict(value = {"accounts", "balances"}, key = "#accountId")
    public AccountResponse freezeAccount(String accountId) {
        log.info("Freezing account: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found: " + accountId));

        account.setStatus(AccountStatus.FROZEN);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        // Evict cache
        evictAccountCache(accountId);

        return AccountResponse.builder()
                .accountId(accountId)
                .accountNumber(account.getAccountNumber())
                .status(AccountStatus.FROZEN)
                .type(account.getAccountType())
                .currency(account.getCurrency())
                .updatedAt(account.getUpdatedAt())
                .message("Account frozen successfully")
                .build();
    }

    @Override
    @CacheEvict(value = {"accounts", "balances"}, key = "#accountId")
    public AccountResponse unfreezeAccount(String accountId) {
        log.info("Unfreezing account: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found: " + accountId));

        account.setStatus(AccountStatus.ACTIVE);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        // Evict cache
        evictAccountCache(accountId);

        return AccountResponse.builder()
                .accountId(accountId)
                .accountNumber(account.getAccountNumber())
                .status(AccountStatus.ACTIVE)
                .type(account.getAccountType())
                .currency(account.getCurrency())
                .updatedAt(account.getUpdatedAt())
                .message("Account unfrozen successfully")
                .build();
    }

    private BigDecimal getReservedAmount(String accountId) {
        String key = "reserved:" + accountId;
        Object reserved = redisTemplate.opsForValue().get(key);
        return reserved != null ? new BigDecimal(reserved.toString()) : BigDecimal.ZERO;
    }

    public void reserveAmount(String accountId, BigDecimal amount) {
        String key = "reserved:" + accountId;
        redisTemplate.opsForValue().increment(key, amount.doubleValue());
        redisTemplate.expire(key, Duration.ofMinutes(30));
    }

    public void releaseReservedAmount(String accountId, BigDecimal amount) {
        String key = "reserved:" + accountId;
        redisTemplate.opsForValue().increment(key, -amount.doubleValue());
    }

    @CacheEvict(value = {"accounts", "balances", "balance-checks"}, key = "#accountId")
    public void evictAccountCache(String accountId) {
        log.debug("Evicting cache for account: {}", accountId);
    }
}
