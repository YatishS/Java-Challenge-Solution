package com.db.awmd.challenge.exception;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by sonk01 on 05/12/17.
 */
public class Response {

    List<Error> errors = new ArrayList<>();

    public List<Error> getErrors() {
        return errors;
    }
}
