package org.example.bankingtransactionapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.bankingtransactionapi.model.enums.AccountStatus;
import org.example.bankingtransactionapi.model.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private UUID id;
    private String accountNumber;
    private BigDecimal balance;
    private AccountType accountType;
    private AccountStatus status;
    private String ownerUsername;
    private LocalDateTime createdAt;
}
