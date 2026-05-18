package org.example.bankingtransactionapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bankingtransactionapi.dto.request.MoneyRequest;
import org.example.bankingtransactionapi.dto.request.TransferRequest;
import org.example.bankingtransactionapi.dto.response.TransactionResponse;
import org.example.bankingtransactionapi.model.enums.TransactionType;
import org.example.bankingtransactionapi.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Transactions", description = "Deposits, withdrawals, transfers, and history")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/accounts/{accountId}/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Deposit money into an account")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable UUID accountId,
            @Valid @RequestBody MoneyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.deposit(accountId, request, userDetails.getUsername()));
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Withdraw money from an account")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable UUID accountId,
            @Valid @RequestBody MoneyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.withdraw(accountId, request, userDetails.getUsername()));
    }

    @PostMapping("/transactions/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Transfer money between two accounts")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.transfer(request, userDetails.getUsername()));
    }

    @GetMapping("/accounts/{accountId}/transactions")
    @Operation(summary = "Get transaction history with optional date and type filters")
    public ResponseEntity<Page<TransactionResponse>> getTransactionHistory(
            @PathVariable UUID accountId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(transactionService.getTransactionHistory(
                accountId, type, startDate, endDate, pageable, userDetails.getUsername()));
    }

    @GetMapping("/transactions/{transactionId}")
    @Operation(summary = "Get a single transaction by ID")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable UUID transactionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId, userDetails.getUsername()));
    }
}
