package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.utility.ResponseBuilder;
import com.db.awmd.challenge.validator.FundTransferValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;
  private final FundTransferValidator validator;

  @Autowired
  public AccountsController(AccountsService accountsService, FundTransferValidator validator) {
    this.accountsService = accountsService;
    this.validator = validator;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account, Errors errors) {
    log.info("Creating account {}", account);

    if (errors.hasErrors()) {
      return ResponseBuilder.getFieldError(errors);
    }

    try {
      return this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }

  @PutMapping(path = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity makeTransfer(@RequestBody @Valid Transfer transfer, Errors errors) {
    log.info("Making transfer {}", transfer);

    if (errors.hasErrors()) {
      return ResponseBuilder.getFieldError(errors);
    }

    return accountsService.transfer(transfer);

  }

}
