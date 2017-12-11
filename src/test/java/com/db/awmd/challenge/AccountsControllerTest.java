package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.service.AccountsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Before
  public void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  public void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  public void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }

  @Test
  public void transferAmountSuccessfully() throws Exception {
    Account fromAccount = createAccountForTransfer(new BigDecimal("5000.00"));
    Account toAccount = createAccountForTransfer(new BigDecimal("2500.00"));
    Transfer request = new Transfer(fromAccount.getAccountId(), toAccount.getAccountId(), new BigDecimal("2500.00"));

    this.mockMvc.perform(put("/v1/accounts/transfer")
              .contentType(MediaType.APPLICATION_JSON)
              .content(new ObjectMapper().writeValueAsString(request)))
              .andExpect(status().isOk());

    assertThat(fromAccount.getBalance()).isEqualTo("2500.00");
    assertThat(toAccount.getBalance()).isEqualTo("5000.00");
  }

  @Test
  public void transferShouldGiveFieldErrorWhenAccountFromIdIsNULL() throws Exception {
    Account toAccount = createAccountForTransfer(new BigDecimal("2500.00"));
    Transfer request = new Transfer(null, toAccount.getAccountId(), new BigDecimal("2500.00"));

    this.mockMvc.perform(put("/v1/accounts/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[*].code", contains("accountFromId")))
            .andExpect(jsonPath("$.errors[*].description", contains("AccountFrom Id should not be empty or null")))
            .andReturn();
  }

  @Test
  public void transferShouldGiveFieldErrorWhenAccountFromIdIsEmpty() throws Exception {
    Account toAccount = createAccountForTransfer(new BigDecimal("2500.00"));
    Transfer request = new Transfer("", toAccount.getAccountId(), new BigDecimal("2500.00"));

    this.mockMvc.perform(put("/v1/accounts/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[*].code", contains("accountFromId")))
            .andExpect(jsonPath("$.errors[*].description", contains("AccountFrom Id should not be empty or null")))
            .andReturn();
  }

  @Test
  public void transferShouldGiveFieldErrorWhenAccountToIdIsNULL() throws Exception {
    Account fromAccount = createAccountForTransfer(new BigDecimal("5000.00"));
    Transfer request = new Transfer(fromAccount.getAccountId(), null, new BigDecimal("2500.00"));

    this.mockMvc.perform(put("/v1/accounts/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[*].code").value(contains("accountToId")))
            .andExpect(jsonPath("$.errors[*].description", contains("AccountTo Id should not be empty or null")))
            .andReturn();
  }

  @Test
  public void transferShouldGiveFieldErrorWhenAccountToIdIsEmpty() throws Exception {
    Account fromAccount = createAccountForTransfer(new BigDecimal("5000.00"));
    Transfer request = new Transfer(fromAccount.getAccountId(), "", new BigDecimal("2500.00"));

    this.mockMvc.perform(put("/v1/accounts/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[*].code", contains("accountToId")))
            .andExpect(jsonPath("$.errors[*].description", contains("AccountTo Id should not be empty or null")))
            .andReturn();
  }

  @Test
  public void transferShouldGiveFieldErrorWhenAmountIsNull() throws Exception {
    Account fromAccount = createAccountForTransfer(new BigDecimal("5000.00"));
    Account toAccount = createAccountForTransfer(new BigDecimal("2500.00"));
    Transfer request = new Transfer(fromAccount.getAccountId(), toAccount.getAccountId(), null);

    this.mockMvc.perform(put("/v1/accounts/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[*].code", contains("amount")))
            .andExpect(jsonPath("$.errors[*].description", contains("may not be null")))
            .andReturn();
  }

  @Test
  public void transferShouldGiveFieldErrorWhenAmountIsNegative() throws Exception {
    Account fromAccount = createAccountForTransfer(new BigDecimal("5000.00"));
    Account toAccount = createAccountForTransfer(new BigDecimal("2500.00"));
    Transfer request = new Transfer(fromAccount.getAccountId(), toAccount.getAccountId(), new BigDecimal("2500.00").negate());

    this.mockMvc.perform(put("/v1/accounts/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[*].code", contains("amount")))
            .andExpect(jsonPath("$.errors[*].description", contains("Transfer amount must be positive.")))
            .andReturn();
  }

  @Test
  public void transferShouldGiveFieldErrorWhenAllTransferDataIsEmpty() throws Exception {
    Transfer request = new Transfer("", "", null);

    this.mockMvc.perform(put("/v1/accounts/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(3)))
            .andExpect(jsonPath("$.errors[*].code")
                    .value(containsInAnyOrder("accountToId", "accountFromId", "amount")));
//            .andExpect(jsonPath("$.errors[*].description")
//                    .value(containsInAnyOrder("Transfer amount must be positive.", "may not be empty", "may not be null")));
  }

  @Test
  public void transferShouldGiveFieldErrorWhenAllTransferDataIsnull() throws Exception {
    Transfer request = new Transfer(null, null, null);

    this.mockMvc.perform(put("/v1/accounts/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(3)))
            .andExpect(jsonPath("$.errors[*].code", containsInAnyOrder("accountToId", "accountFromId", "amount")));
//            .andExpect(jsonPath("$.errors[*].description").value(containsInAnyOrder("may not be empty", "may not be null", "Transfer amount must be positive.")));
  }

  @Test
  public void transferShouldGiveValidationErrorWhenFromAccountIsNotExist() throws Exception {
    Account toAccount = createAccountForTransfer(new BigDecimal(5000.00));
    Transfer request = new Transfer("acc-2", toAccount.getAccountId(), toAccount.getBalance());

    this.mockMvc.perform(put("/v1/accounts/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[*].code", contains("Account")))
            .andExpect(jsonPath("$.errors[*].description", contains("Account not exist.")));
  }

  @Test
  public void transferShouldGiveValidationErrorWhenToAccountIsNotExist() throws Exception {
    Account fromAccount = createAccountForTransfer(new BigDecimal(5000.00));
    Transfer request = new Transfer(fromAccount.getAccountId(), "acc-2", fromAccount.getBalance());

    this.mockMvc.perform(put("/v1/accounts/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[*].code", contains("Account")))
            .andExpect(jsonPath("$.errors[*].description", contains("Account not exist.")));
  }

  @Test
  public void transferShouldGiveValidationErrorWhenBothAccountIsNotExist() throws Exception {
    Transfer request = new Transfer("acc-1", "acc-2", new BigDecimal(5000.00));

    this.mockMvc.perform(put("/v1/accounts/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[*].code", contains("Account")))
            .andExpect(jsonPath("$.errors[*].description", contains("Account not exist.")));
  }

  @Test
  public void transferShouldGiveValidationErrorWhenAccountIsSame() throws Exception {
    Account account = createAccountForTransfer(new BigDecimal(5000));
    Transfer request = new Transfer(account.getAccountId(), account.getAccountId(), account.getBalance());

    this.mockMvc.perform(put("/v1/accounts/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[*].code", contains("Account")))
            .andExpect(jsonPath("$.errors[*].description", contains("Transfer to self not permitted.")));
  }

  @Test
  public void transferShouldGiveValidationErrorWhenAccountHavingInsufficientAmount() throws Exception {
    Account fromAccount = createAccountForTransfer(new BigDecimal(5000));
    Account toAccount = createAccountForTransfer(new BigDecimal(2500));
    Transfer request = new Transfer(fromAccount.getAccountId(), toAccount.getAccountId(), new BigDecimal(10000.00));

    this.mockMvc.perform(put("/v1/accounts/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[*].code", contains("Fund")));
  }

  private Account createAccountForTransfer(BigDecimal amount) {
    String uniqueAccountId = Long.toString(System.currentTimeMillis() + amount.intValue());
    Account account = new Account(uniqueAccountId, amount);
    this.accountsService.createAccount(account);
    return account;
  }
}
