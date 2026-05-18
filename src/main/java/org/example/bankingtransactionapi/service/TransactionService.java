package org.example.bankingtransactionapi.service;

import lombok.RequiredArgsConstructor;
import org.example.bankingtransactionapi.dto.request.MoneyRequest;
import org.example.bankingtransactionapi.dto.request.TransferRequest;
import org.example.bankingtransactionapi.dto.response.TransactionResponse;
import org.example.bankingtransactionapi.exception.AccountFrozenException;
import org.example.bankingtransactionapi.exception.InsufficientFundsException;
import org.example.bankingtransactionapi.exception.ResourceNotFoundException;
import org.example.bankingtransactionapi.mapper.TransactionMapper;
import org.example.bankingtransactionapi.model.BankAccount;
import org.example.bankingtransactionapi.model.Transaction;
import org.example.bankingtransactionapi.model.enums.AccountStatus;
import org.example.bankingtransactionapi.model.enums.TransactionStatus;
import org.example.bankingtransactionapi.model.enums.TransactionType;
import org.example.bankingtransactionapi.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountService bankAccountService;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponse deposit(UUID accountId, MoneyRequest request, String username) {
        BankAccount account = bankAccountService.findAccountById(accountId);
        bankAccountService.verifyOwnership(account, username);
        requireActive(account);

        account.setBalance(account.getBalance().add(request.getAmount()));

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .toAccount(account)
                .description(request.getDescription())
                .status(TransactionStatus.COMPLETED)
                .build();

        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse withdraw(UUID accountId, MoneyRequest request, String username) {
        BankAccount account = bankAccountService.findAccountById(accountId);
        bankAccountService.verifyOwnership(account, username);
        requireActive(account);
        requireSufficientFunds(account, request.getAmount());

        account.setBalance(account.getBalance().subtract(request.getAmount()));

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .fromAccount(account)
                .description(request.getDescription())
                .status(TransactionStatus.COMPLETED)
                .build();

        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest request, String username) {
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        BankAccount fromAccount = bankAccountService.findAccountById(request.getFromAccountId());
        bankAccountService.verifyOwnership(fromAccount, username);
        requireActive(fromAccount);
        requireSufficientFunds(fromAccount, request.getAmount());

        BankAccount toAccount = bankAccountService.findAccountById(request.getToAccountId());
        if (toAccount.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException("Destination account is frozen");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .description(request.getDescription())
                .status(TransactionStatus.COMPLETED)
                .build();

        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionHistory(
            UUID accountId, TransactionType type,
            LocalDate startDate, LocalDate endDate,
            Pageable pageable, String username) {

        BankAccount account = bankAccountService.findAccountById(accountId);
        bankAccountService.verifyOwnership(account, username);

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;

        return transactionRepository
                .findByAccountAndFilters(account, type, start, end, pageable)
                .map(transactionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID transactionId, String username) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));

        boolean involved = (transaction.getFromAccount() != null
                && transaction.getFromAccount().getOwner().getUsername().equals(username))
                || (transaction.getToAccount() != null
                && transaction.getToAccount().getOwner().getUsername().equals(username));

        if (!involved) {
            throw new org.example.bankingtransactionapi.exception.UnauthorizedAccessException(
                    "You do not have access to this transaction");
        }

        return transactionMapper.toResponse(transaction);
    }

    private void requireActive(BankAccount account) {
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException("Account " + account.getAccountNumber() + " is frozen");
        }
        if (account.getStatus() == AccountStatus.INACTIVE) {
            throw new AccountFrozenException("Account " + account.getAccountNumber() + " is inactive");
        }
    }

    private void requireSufficientFunds(BankAccount account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds. Available: " + account.getBalance() + ", required: " + amount);
        }
    }
}
