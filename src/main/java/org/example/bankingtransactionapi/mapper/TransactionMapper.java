package org.example.bankingtransactionapi.mapper;

import org.example.bankingtransactionapi.dto.response.TransactionResponse;
import org.example.bankingtransactionapi.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .fromAccountNumber(transaction.getFromAccount() != null
                        ? transaction.getFromAccount().getAccountNumber() : null)
                .toAccountNumber(transaction.getToAccount() != null
                        ? transaction.getToAccount().getAccountNumber() : null)
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
