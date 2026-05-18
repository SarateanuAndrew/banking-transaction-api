package org.example.bankingtransactionapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bankingtransactionapi.dto.request.CreateAccountRequest;
import org.example.bankingtransactionapi.dto.response.AccountResponse;
import org.example.bankingtransactionapi.service.BankAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Bank Accounts", description = "Account management")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    @Operation(summary = "Create a new bank account")
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bankAccountService.createAccount(request, userDetails.getUsername()));
    }

    @GetMapping
    @Operation(summary = "Get all accounts for the authenticated user")
    public ResponseEntity<List<AccountResponse>> getUserAccounts(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bankAccountService.getUserAccounts(userDetails.getUsername()));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account details by ID")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bankAccountService.getAccountById(accountId, userDetails.getUsername()));
    }

    @GetMapping("/{accountId}/balance")
    @Operation(summary = "Get account balance")
    public ResponseEntity<Map<String, BigDecimal>> getBalance(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal UserDetails userDetails) {
        BigDecimal balance = bankAccountService.getBalance(accountId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("balance", balance));
    }
}
