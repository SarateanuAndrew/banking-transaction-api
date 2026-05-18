package org.example.bankingtransactionapi.mapper;

import org.example.bankingtransactionapi.dto.response.AccountResponse;
import org.example.bankingtransactionapi.model.BankAccount;
import org.springframework.stereotype.Component;

@Component
public class BankAccountMapper {

    public AccountResponse toResponse(BankAccount account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .ownerUsername(account.getOwner().getUsername())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
