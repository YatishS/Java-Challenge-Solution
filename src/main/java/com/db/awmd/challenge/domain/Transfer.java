package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Created by sonk01 on 05/12/17.
 */
@Data
public class Transfer {

    @NotEmpty(message = "AccountFrom Id should not be empty or null")
    private String accountFromId;

    @NotEmpty(message = "AccountTo Id should not be empty or null")
    private String accountToId;

    @NotNull
    @Min(value = 1, message = "Transfer amount must be positive.")
    private BigDecimal amount;

    @JsonCreator
    public Transfer(@JsonProperty("accountFromId") String accountFromId,
                    @JsonProperty("accountToId") String accountToId,
                    @JsonProperty("amount") BigDecimal amount){
        this.accountFromId = accountFromId;
        this.accountToId = accountToId;
        this.amount = amount;
    }

}
