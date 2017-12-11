package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientFundException;
import com.db.awmd.challenge.exception.NoAccountFoundException;
import com.db.awmd.challenge.exception.SameAccountFoundException;
import com.db.awmd.challenge.service.AccountsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  @Test
  public void transferAmountSuccessfully() throws Exception {
    Account fromAccount = createAccountForTransfer(new BigDecimal("5000.00"));
    Account toAccount = createAccountForTransfer(new BigDecimal("2500.00"));
    Transfer transfer = new Transfer(fromAccount.getAccountId(), toAccount.getAccountId(), new BigDecimal("2500.00"));

    this.accountsService.transfer(transfer);

    assertThat(fromAccount.getBalance()).isEqualTo("2500.00");
    assertThat(toAccount.getBalance()).isEqualTo("5000.00");
  }


  @Test
  public void transferShouldGiveValidationErrorWhenFromAccountIsNotExist() throws Exception {
    Account toAccount = createAccountForTransfer(new BigDecimal(5000.00));
    Transfer transfer = new Transfer("acc-2", toAccount.getAccountId(), toAccount.getBalance());

    try {
      this.accountsService.transfer(transfer);
    } catch (NoAccountFoundException e) {
      assertThat(e.getMessage()).isEqualTo("From-Account [" + transfer.getAccountFromId() + "] not exist.");
    }
  }

  @Test
  public void transferShouldGiveValidationErrorWhenToAccountIsNotExist() throws Exception {
    Account fromAccount = createAccountForTransfer(new BigDecimal(5000.00));
    Transfer transfer = new Transfer(fromAccount.getAccountId(), "acc-2", fromAccount.getBalance());

    try {
      this.accountsService.transfer(transfer);
    } catch (NoAccountFoundException e) {
      assertThat(e.getMessage()).isEqualTo("From-Account [" + transfer.getAccountToId() + "] not exist.");
    }
  }

  @Test
  public void transferShouldGiveValidationErrorWhenBothAccountIsNotExist() throws Exception {
    Transfer transfer = new Transfer("acc-1", "acc-2", new BigDecimal(5000.00));

    try {
      this.accountsService.transfer(transfer);
    } catch (NoAccountFoundException e) {
      assertThat(e.getMessage()).isEqualTo("From-Account [" + transfer.getAccountFromId() + "] not exist.");
    }

  }

  @Test
  public void transferShouldGiveValidationErrorWhenAccountIsSame() throws Exception {
    Account account = createAccountForTransfer(new BigDecimal(5000));
    Transfer transfer = new Transfer(account.getAccountId(), account.getAccountId(), account.getBalance());

    try {
      this.accountsService.transfer(transfer);
    } catch (SameAccountFoundException e) {
      assertThat(e.getMessage()).isEqualTo("Transfer to self not permitted.");
    }
  }

  @Test
  public void transferShouldGiveValidationErrorWhenAccountHavingInsufficientAmount() throws Exception {
    Account fromAccount = createAccountForTransfer(new BigDecimal(5000));
    Account toAccount = createAccountForTransfer(new BigDecimal(2500));
    Transfer transfer = new Transfer(fromAccount.getAccountId(), toAccount.getAccountId(), new BigDecimal(10000.00));

    try {
      this.accountsService.transfer(transfer);
    } catch (InsufficientFundException e) {
      assertThat(e.getMessage()).isEqualTo("Insufficient funds on account [" + fromAccount.getAccountId() +
              "], available balance= "+ fromAccount.getBalance());
    }

  }

  private Account createAccountForTransfer(BigDecimal amount) {
    String uniqueAccountId = Long.toString(System.currentTimeMillis() + amount.intValue());
    Account account = new Account(uniqueAccountId, amount);
    this.accountsService.createAccount(account);
    return account;
  }
}
