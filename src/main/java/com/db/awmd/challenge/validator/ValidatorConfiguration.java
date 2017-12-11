package com.db.awmd.challenge.validator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedList;
import java.util.List;

@Configuration
public class ValidatorConfiguration {

    @Bean
    public List<Validator> Validators() {
        return new LinkedList<>(getValidators());
    }

    private List<Validator> getValidators() {
        List<Validator> validators = new LinkedList<>();
        validators.add(new InvalidAccountValidator());
        validators.add(new SameAccountValidator());
        validators.add(new InsufficientFundValidator());
        return validators;
    }
}
