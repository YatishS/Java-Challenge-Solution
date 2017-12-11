package com.db.awmd.challenge.validator;

import com.db.awmd.challenge.domain.Account;

import java.util.List;

public interface Validator {

    void validate(List<Account> accounts);
}
