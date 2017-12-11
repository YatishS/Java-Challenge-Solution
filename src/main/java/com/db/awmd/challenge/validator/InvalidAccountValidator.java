package com.db.awmd.challenge.validator;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.NoAccountFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Created by sonk01 on 07/12/17.
 */
@Component
@Order(1)
public class InvalidAccountValidator implements Validator{

    @Override
    public void validate(List<Account> accounts) {
        accounts.forEach( account -> {
            Optional.ofNullable(account)
                    .orElseThrow(() -> new NoAccountFoundException("Account not exist."));
        });
    }
}
