package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.Response;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.utility.ResponseBuilder;
import com.db.awmd.challenge.validator.FundTransferValidator;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private FundTransferValidator validator;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
    this.accountsRepository = accountsRepository;
    this.notificationService = notificationService;
  }

  public ResponseEntity createAccount(Account account) {
    this.accountsRepository.createAccount(account);
    return ResponseBuilder.getSuccessResponse("Account [" + account.getAccountId() + "] opened successfully.", HttpStatus.CREATED);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public ResponseEntity transfer(Transfer transfer) {
    final BigDecimal amountToBeDeposit = transfer.getAmount();

    final Account accountFrom = accountsRepository.getAccount(transfer.getAccountFromId());
    if(accountFrom != null) {
      accountFrom.setAmountToBeDeposit(amountToBeDeposit.negate());
    }

    final Account accountTo = accountsRepository.getAccount(transfer.getAccountToId());
    if(accountTo != null) {
      accountTo.setAmountToBeDeposit(amountToBeDeposit);
    }

    Response response = validate(accountFrom, accountTo);
    if(response != null && response.getErrors().size() > 0) {
      return ResponseBuilder.getErrorResponse(response);
    }

    if(accountsRepository.transfer(Arrays.asList(accountFrom, accountTo))) {
      sendTransferNotification(accountFrom, accountTo);
    }

    return ResponseBuilder.getSuccessResponse("Amount transferred successfully.", HttpStatus.OK);
  }

  private Response validate(Account accountFrom, Account accountTo) {
    return validator.validate(Arrays.asList(accountFrom, accountTo));
  }

  private void sendTransferNotification(Account accountFrom, Account accountTo) {
    notificationService.notifyAboutTransfer(accountFrom, "Transfer completed successfully of amount[" +
                    accountTo.getAmountToBeDeposit() + "] to account[" +  accountTo.getAccountId() + "].");

    notificationService.notifyAboutTransfer(accountTo, "Account [" + accountFrom.getAccountId() +
            "has transferred amount[" + accountTo.getAmountToBeDeposit() + "] into your account.");
  }

}
