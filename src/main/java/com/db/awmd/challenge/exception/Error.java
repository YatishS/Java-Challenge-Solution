package com.db.awmd.challenge.exception;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by sonk01 on 05/12/17.
 */
public class Error {

    @JsonProperty
    private String code;
    @JsonProperty
    private String description;

    public Error(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
