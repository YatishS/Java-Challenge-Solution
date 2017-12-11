package com.db.awmd.challenge.validator;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.SameAccountFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by sonk01 on 07/12/17.
 */
@Component
@Order(2)
public class SameAccountValidator implements Validator{

    @Override
    public void validate(List<Account> accounts) {
        if (isSameAccount(accounts.get(0), accounts.get(1))){
            throw new SameAccountFoundException("Transfer to self not permitted.");
        }
    }

    private boolean isSameAccount(final Account fromAccount, final Account toAccount) {
        return fromAccount.getAccountId().equals(toAccount.getAccountId());
    }
}
