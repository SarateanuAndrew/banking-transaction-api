package org.example.bankingtransactionapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bankingtransactionapi.dto.request.CreateAccountRequest;
import org.example.bankingtransactionapi.dto.request.LoginRequest;
import org.example.bankingtransactionapi.dto.request.MoneyRequest;
import org.example.bankingtransactionapi.dto.request.RegisterRequest;
import org.example.bankingtransactionapi.model.enums.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TransactionIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String jwtToken;
    private String accountId;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("integrationuser_" + System.nanoTime());
        register.setEmail("integration_" + System.nanoTime() + "@example.com");
        register.setPassword("password123");

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn();

        String registerBody = registerResult.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(registerBody).get("token").asText();

        CreateAccountRequest createAccount = new CreateAccountRequest();
        createAccount.setAccountType(AccountType.CHECKING);

        MvcResult accountResult = mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccount)))
                .andExpect(status().isCreated())
                .andReturn();

        String accountBody = accountResult.getResponse().getContentAsString();
        accountId = objectMapper.readTree(accountBody).get("id").asText();
    }

    @Test
    void deposit_ShouldIncreaseBalance() throws Exception {
        MoneyRequest deposit = new MoneyRequest();
        deposit.setAmount(new BigDecimal("500.00"));
        deposit.setDescription("Initial deposit");

        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/deposit")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deposit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void withdraw_WithInsufficientFunds_ShouldReturn422() throws Exception {
        MoneyRequest withdraw = new MoneyRequest();
        withdraw.setAmount(new BigDecimal("9999.00"));

        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/withdraw")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdraw)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void getTransactionHistory_ShouldReturnPagedResults() throws Exception {
        MoneyRequest deposit = new MoneyRequest();
        deposit.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/deposit")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deposit)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/accounts/" + accountId + "/transactions")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void accessAccountWithoutToken_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isForbidden());
    }

    @Test
    void register_WithInvalidEmail_ShouldReturn400() throws Exception {
        RegisterRequest bad = new RegisterRequest();
        bad.setUsername("user");
        bad.setEmail("not-an-email");
        bad.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }
}
