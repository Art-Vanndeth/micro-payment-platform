package com.pipay.account.controller;

import com.pipay.account.dto.AccountResponse;
import com.pipay.account.dto.BalanceCheckResponse;
import com.pipay.account.dto.BalanceResponse;
import com.pipay.account.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@Slf4j
@Validated
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/{accountId}/balance-check")
    public ResponseEntity<BalanceCheckResponse> checkBalance(
            @PathVariable String accountId,
            @RequestBody Map<String, BigDecimal> request) {

        BigDecimal amount = request.get("amount");
        BalanceCheckResponse response = accountService.checkBalance(accountId, amount);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String accountId) {
        BalanceResponse response = accountService.getBalance(accountId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountId}/freeze")
    public ResponseEntity<AccountResponse> freezeAccount(@PathVariable String accountId) {
        AccountResponse response = accountService.freezeAccount(accountId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountId}/unfreeze")
    public ResponseEntity<AccountResponse> unfreezeAccount(@PathVariable String accountId) {
        AccountResponse response = accountService.unfreezeAccount(accountId);
        return ResponseEntity.ok(response);
    }
}
