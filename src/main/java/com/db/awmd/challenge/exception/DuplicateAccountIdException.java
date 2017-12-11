package com.db.awmd.challenge.exception;

/**
 * Created by sonk01 on 05/12/17.
 */
public class DuplicateAccountIdException extends RuntimeException {

  public DuplicateAccountIdException(String message) {
    super(message);
  }
}
