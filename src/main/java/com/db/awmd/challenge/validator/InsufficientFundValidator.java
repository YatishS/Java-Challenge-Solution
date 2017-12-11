package com.db.awmd.challenge.validator;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.InsufficientFundException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by sonk01 on 07/12/17.
 */
@Component
@Order(3)
public class InsufficientFundValidator implements Validator{

    @Override
    public void validate(List<Account> accounts) {
        Account fromAccount = accounts.get(0);
        if (!isInsufficientFunds(fromAccount, accounts.get(1).getAmountToBeDeposit())){
            throw new InsufficientFundException("Insufficient funds on account [" + fromAccount.getAccountId() +
                    "], available balance= "+ fromAccount.getBalance());
        }
    }

    private boolean isInsufficientFunds(final Account fromAccount, final BigDecimal amount) {
        BigDecimal zeroValue = BigDecimal.ZERO;

        if(null == fromAccount.getBalance() || fromAccount.getBalance().compareTo(zeroValue) == 0) {
            return false;
        }
        return fromAccount.getBalance().subtract(amount).compareTo(zeroValue) >= 0;
    }
}
