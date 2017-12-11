package com.db.awmd.challenge.exception;

/**
 * Created by sonk01 on 05/12/17.
 */
public class SameAccountFoundException extends RuntimeException{

    public SameAccountFoundException(String message) {
        super(message);
    }
}
