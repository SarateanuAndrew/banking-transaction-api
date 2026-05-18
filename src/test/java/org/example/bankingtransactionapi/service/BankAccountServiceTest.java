package org.example.bankingtransactionapi.service;

import org.example.bankingtransactionapi.dto.request.CreateAccountRequest;
import org.example.bankingtransactionapi.dto.response.AccountResponse;
import org.example.bankingtransactionapi.exception.ResourceNotFoundException;
import org.example.bankingtransactionapi.exception.UnauthorizedAccessException;
import org.example.bankingtransactionapi.mapper.BankAccountMapper;
import org.example.bankingtransactionapi.model.BankAccount;
import org.example.bankingtransactionapi.model.User;
import org.example.bankingtransactionapi.model.enums.AccountStatus;
import org.example.bankingtransactionapi.model.enums.AccountType;
import org.example.bankingtransactionapi.model.enums.Role;
import org.example.bankingtransactionapi.repository.BankAccountRepository;
import org.example.bankingtransactionapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock private BankAccountRepository bankAccountRepository;
    @Mock private UserRepository userRepository;
    @Mock private BankAccountMapper bankAccountMapper;

    @InjectMocks private BankAccountService bankAccountService;

    private User user;
    private BankAccount account;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("john")
                .email("john@example.com")
                .role(Role.ROLE_USER)
                .build();

        account = BankAccount.builder()
                .id(UUID.randomUUID())
                .accountNumber("1234567890")
                .balance(new BigDecimal("500.00"))
                .owner(user)
                .accountType(AccountType.CHECKING)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void createAccount_ShouldReturnAccountResponse() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setAccountType(AccountType.CHECKING);

        AccountResponse expected = AccountResponse.builder()
                .accountNumber("1234567890")
                .accountType(AccountType.CHECKING)
                .status(AccountStatus.ACTIVE)
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(bankAccountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(account);
        when(bankAccountMapper.toResponse(account)).thenReturn(expected);

        AccountResponse response = bankAccountService.createAccount(request, "john");

        assertThat(response.getAccountType()).isEqualTo(AccountType.CHECKING);
        assertThat(response.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void getBalance_ShouldReturnBalance_WhenAccountBelongsToUser() {
        when(bankAccountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        BigDecimal balance = bankAccountService.getBalance(account.getId(), "john");

        assertThat(balance).isEqualByComparingTo("500.00");
    }

    @Test
    void getBalance_WhenAccountNotFound_ShouldThrowResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(bankAccountRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankAccountService.getBalance(unknownId, "john"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getBalance_WhenNotOwner_ShouldThrowUnauthorizedAccessException() {
        when(bankAccountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> bankAccountService.getBalance(account.getId(), "other_user"))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void getUserAccounts_ShouldReturnAllAccountsForUser() {
        AccountResponse accountResponse = AccountResponse.builder()
                .accountNumber("1234567890")
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(bankAccountRepository.findByOwner(user)).thenReturn(List.of(account));
        when(bankAccountMapper.toResponse(account)).thenReturn(accountResponse);

        List<AccountResponse> accounts = bankAccountService.getUserAccounts("john");

        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getAccountNumber()).isEqualTo("1234567890");
    }

    @Test
    void getAccountById_WhenNotOwner_ShouldThrowUnauthorizedAccessException() {
        when(bankAccountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> bankAccountService.getAccountById(account.getId(), "hacker"))
                .isInstanceOf(UnauthorizedAccessException.class);
    }
}
