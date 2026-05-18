package org.example.bankingtransactionapi.service;

import org.example.bankingtransactionapi.dto.request.MoneyRequest;
import org.example.bankingtransactionapi.dto.request.TransferRequest;
import org.example.bankingtransactionapi.dto.response.TransactionResponse;
import org.example.bankingtransactionapi.exception.AccountFrozenException;
import org.example.bankingtransactionapi.exception.InsufficientFundsException;
import org.example.bankingtransactionapi.mapper.TransactionMapper;
import org.example.bankingtransactionapi.model.BankAccount;
import org.example.bankingtransactionapi.model.Transaction;
import org.example.bankingtransactionapi.model.User;
import org.example.bankingtransactionapi.model.enums.AccountStatus;
import org.example.bankingtransactionapi.model.enums.AccountType;
import org.example.bankingtransactionapi.model.enums.Role;
import org.example.bankingtransactionapi.model.enums.TransactionStatus;
import org.example.bankingtransactionapi.model.enums.TransactionType;
import org.example.bankingtransactionapi.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private BankAccountService bankAccountService;
    @Mock private TransactionMapper transactionMapper;

    @InjectMocks private TransactionService transactionService;

    private User user;
    private BankAccount account;
    private BankAccount targetAccount;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("john")
                .role(Role.ROLE_USER)
                .build();

        account = BankAccount.builder()
                .id(UUID.randomUUID())
                .accountNumber("1111111111")
                .balance(new BigDecimal("1000.00"))
                .owner(user)
                .accountType(AccountType.CHECKING)
                .status(AccountStatus.ACTIVE)
                .build();

        User other = User.builder().id(UUID.randomUUID()).username("jane").role(Role.ROLE_USER).build();
        targetAccount = BankAccount.builder()
                .id(UUID.randomUUID())
                .accountNumber("2222222222")
                .balance(new BigDecimal("200.00"))
                .owner(other)
                .accountType(AccountType.CHECKING)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void deposit_ShouldIncreaseAccountBalance() {
        MoneyRequest request = new MoneyRequest();
        request.setAmount(new BigDecimal("250.00"));

        Transaction saved = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionType(TransactionType.DEPOSIT)
                .amount(new BigDecimal("250.00"))
                .toAccount(account)
                .status(TransactionStatus.COMPLETED)
                .build();

        TransactionResponse expected = TransactionResponse.builder()
                .transactionType(TransactionType.DEPOSIT)
                .amount(new BigDecimal("250.00"))
                .build();

        when(bankAccountService.findAccountById(account.getId())).thenReturn(account);
        doNothing().when(bankAccountService).verifyOwnership(account, "john");
        when(transactionRepository.save(any())).thenReturn(saved);
        when(transactionMapper.toResponse(saved)).thenReturn(expected);

        TransactionResponse response = transactionService.deposit(account.getId(), request, "john");

        assertThat(account.getBalance()).isEqualByComparingTo("1250.00");
        assertThat(response.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    void withdraw_ShouldDecreaseAccountBalance() {
        MoneyRequest request = new MoneyRequest();
        request.setAmount(new BigDecimal("300.00"));

        Transaction saved = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("300.00"))
                .fromAccount(account)
                .status(TransactionStatus.COMPLETED)
                .build();

        TransactionResponse expected = TransactionResponse.builder()
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("300.00"))
                .build();

        when(bankAccountService.findAccountById(account.getId())).thenReturn(account);
        doNothing().when(bankAccountService).verifyOwnership(account, "john");
        when(transactionRepository.save(any())).thenReturn(saved);
        when(transactionMapper.toResponse(saved)).thenReturn(expected);

        TransactionResponse response = transactionService.withdraw(account.getId(), request, "john");

        assertThat(account.getBalance()).isEqualByComparingTo("700.00");
        assertThat(response.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
    }

    @Test
    void withdraw_WhenInsufficientFunds_ShouldThrowInsufficientFundsException() {
        MoneyRequest request = new MoneyRequest();
        request.setAmount(new BigDecimal("5000.00"));

        when(bankAccountService.findAccountById(account.getId())).thenReturn(account);
        doNothing().when(bankAccountService).verifyOwnership(account, "john");

        assertThatThrownBy(() -> transactionService.withdraw(account.getId(), request, "john"))
                .isInstanceOf(InsufficientFundsException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transfer_ShouldMoveMoneyBetweenAccounts() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(account.getId());
        request.setToAccountId(targetAccount.getId());
        request.setAmount(new BigDecimal("400.00"));

        Transaction saved = Transaction.builder()
                .id(UUID.randomUUID())
                .transactionType(TransactionType.TRANSFER)
                .amount(new BigDecimal("400.00"))
                .fromAccount(account)
                .toAccount(targetAccount)
                .status(TransactionStatus.COMPLETED)
                .build();

        TransactionResponse expected = TransactionResponse.builder()
                .transactionType(TransactionType.TRANSFER)
                .amount(new BigDecimal("400.00"))
                .build();

        when(bankAccountService.findAccountById(account.getId())).thenReturn(account);
        doNothing().when(bankAccountService).verifyOwnership(account, "john");
        when(bankAccountService.findAccountById(targetAccount.getId())).thenReturn(targetAccount);
        when(transactionRepository.save(any())).thenReturn(saved);
        when(transactionMapper.toResponse(saved)).thenReturn(expected);

        TransactionResponse response = transactionService.transfer(request, "john");

        assertThat(account.getBalance()).isEqualByComparingTo("600.00");
        assertThat(targetAccount.getBalance()).isEqualByComparingTo("600.00");
        assertThat(response.getTransactionType()).isEqualTo(TransactionType.TRANSFER);
    }

    @Test
    void transfer_WhenInsufficientFunds_ShouldThrowInsufficientFundsException() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(account.getId());
        request.setToAccountId(targetAccount.getId());
        request.setAmount(new BigDecimal("9999.00"));

        when(bankAccountService.findAccountById(account.getId())).thenReturn(account);
        doNothing().when(bankAccountService).verifyOwnership(account, "john");

        assertThatThrownBy(() -> transactionService.transfer(request, "john"))
                .isInstanceOf(InsufficientFundsException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void deposit_WhenAccountFrozen_ShouldThrowAccountFrozenException() {
        account.setStatus(AccountStatus.FROZEN);
        MoneyRequest request = new MoneyRequest();
        request.setAmount(new BigDecimal("100.00"));

        when(bankAccountService.findAccountById(account.getId())).thenReturn(account);
        doNothing().when(bankAccountService).verifyOwnership(account, "john");

        assertThatThrownBy(() -> transactionService.deposit(account.getId(), request, "john"))
                .isInstanceOf(AccountFrozenException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transfer_ToSameAccount_ShouldThrowIllegalArgumentException() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(account.getId());
        request.setToAccountId(account.getId());
        request.setAmount(new BigDecimal("100.00"));

        assertThatThrownBy(() -> transactionService.transfer(request, "john"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
