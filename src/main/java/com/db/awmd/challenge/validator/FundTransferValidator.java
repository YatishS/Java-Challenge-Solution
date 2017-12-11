package com.db.awmd.challenge.validator;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.Error;
import com.db.awmd.challenge.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by sonk01 on 05/12/17.
 */
@Component
public class FundTransferValidator {

    @Autowired
    private List<Validator> validators;

    public Response validate(final List<Account> accounts) {
        Response response = new Response();
        try {
            validators.forEach(validator -> {
                    validator.validate(accounts);
            });
        } catch (NoAccountFoundException e) {
            Error error = new Error("Account", e.getMessage());
            response.getErrors().add(error);
        } catch (SameAccountFoundException e) {
            Error error = new Error("Account", e.getMessage());
            response.getErrors().add(error);
        } catch (InsufficientFundException e) {
            Error error = new Error("Fund", e.getMessage());
            response.getErrors().add(error);
        }

        return response;
    }
}
