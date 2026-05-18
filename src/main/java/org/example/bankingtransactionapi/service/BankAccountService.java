package org.example.bankingtransactionapi.service;

import lombok.RequiredArgsConstructor;
import org.example.bankingtransactionapi.dto.request.CreateAccountRequest;
import org.example.bankingtransactionapi.dto.response.AccountResponse;
import org.example.bankingtransactionapi.exception.ResourceNotFoundException;
import org.example.bankingtransactionapi.exception.UnauthorizedAccessException;
import org.example.bankingtransactionapi.mapper.BankAccountMapper;
import org.example.bankingtransactionapi.model.BankAccount;
import org.example.bankingtransactionapi.model.User;
import org.example.bankingtransactionapi.model.enums.AccountStatus;
import org.example.bankingtransactionapi.repository.BankAccountRepository;
import org.example.bankingtransactionapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final BankAccountMapper bankAccountMapper;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, String username) {
        User owner = findUserByUsername(username);

        BankAccount account = BankAccount.builder()
                .accountNumber(generateAccountNumber())
                .balance(BigDecimal.ZERO)
                .owner(owner)
                .accountType(request.getAccountType())
                .status(AccountStatus.ACTIVE)
                .build();

        return bankAccountMapper.toResponse(bankAccountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(UUID accountId, String username) {
        BankAccount account = findAccountById(accountId);
        verifyOwnership(account, username);
        return bankAccountMapper.toResponse(account);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID accountId, String username) {
        BankAccount account = findAccountById(accountId);
        verifyOwnership(account, username);
        return account.getBalance();
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getUserAccounts(String username) {
        User owner = findUserByUsername(username);
        return bankAccountRepository.findByOwner(owner).stream()
                .map(bankAccountMapper::toResponse)
                .toList();
    }

    public BankAccount findAccountById(UUID accountId) {
        return bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
    }

    public void verifyOwnership(BankAccount account, String username) {
        if (!account.getOwner().getUsername().equals(username)) {
            throw new UnauthorizedAccessException("You do not have access to this account");
        }
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private String generateAccountNumber() {
        String candidate;
        do {
            candidate = String.valueOf(1_000_000_000L + ThreadLocalRandom.current().nextLong(9_000_000_000L));
        } while (bankAccountRepository.existsByAccountNumber(candidate));
        return candidate;
    }
}
