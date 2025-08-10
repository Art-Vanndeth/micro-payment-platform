package com.pipay.account.controller;

import com.pipay.account.dto.AccountResponse;
import com.pipay.account.dto.BalanceCheckResponse;
import com.pipay.account.dto.BalanceResponse;
import com.pipay.account.dto.AccountValidationResponse;
import com.pipay.account.dto.TransferResponse;
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

    @PostMapping("/{accountNumber}/balance-check")
    public ResponseEntity<BalanceCheckResponse> checkBalance(
            @PathVariable String accountNumber,
            @RequestBody Map<String, BigDecimal> request) {

        BigDecimal amount = request.get("amount");
        BalanceCheckResponse response = accountService.checkBalance(accountNumber, amount);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String accountNumber) {
        BalanceResponse response = accountService.getBalance(accountNumber);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{accountNumber}/freeze")
    public ResponseEntity<AccountResponse> freezeAccount(@PathVariable String accountNumber) {
        AccountResponse response = accountService.freezeAccount(accountNumber);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{accountNumber}/unfreeze")
    public ResponseEntity<AccountResponse> unfreezeAccount(@PathVariable String accountNumber) {
        AccountResponse response = accountService.unfreezeAccount(accountNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNumber}/validate")
    public ResponseEntity<AccountValidationResponse> validateAccount(@PathVariable String accountNumber) {
        log.info("Validating account: {}", accountNumber);
        AccountValidationResponse response = accountService.validateAccount(accountNumber);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> processTransfer(@RequestBody Map<String, Object> request) {
        log.info("Processing transfer request: {}", request);

        String sourceAccountNumber = (String) request.get("sourceAccountNumber");
        String recipientAccountNumber = (String) request.get("recipientAccountNumber");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        TransferResponse response = accountService.processTransfer(sourceAccountNumber, recipientAccountNumber, amount);
        return ResponseEntity.ok(response);
    }
}
