package org.example.bankingtransactionapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bankingtransactionapi.model.enums.TransactionStatus;
import org.example.bankingtransactionapi.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private UUID id;
    private TransactionType transactionType;
    private BigDecimal amount;
    private String fromAccountNumber;
    private String toAccountNumber;
    private String description;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
