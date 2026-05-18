package org.example.bankingtransactionapi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.bankingtransactionapi.model.enums.AccountType;

@Data
public class CreateAccountRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;
}
