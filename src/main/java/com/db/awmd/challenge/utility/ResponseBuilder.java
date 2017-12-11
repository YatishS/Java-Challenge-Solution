package com.db.awmd.challenge.utility;

import com.db.awmd.challenge.exception.Error;
import com.db.awmd.challenge.exception.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.List;

/**
 * Created by sonk01 on 07/12/17.
 */
public class ResponseBuilder {

    public static ResponseEntity getFieldError(Errors errors) {
        Response response = new Response();
        final List<FieldError> fieldErrorList = errors.getFieldErrors();

        for (FieldError fieldError : fieldErrorList) {
            addError(response, fieldError.getField(), fieldError.getDefaultMessage());
        }

        return getErrorResponse(response);
    }

    private static void addError(Response response, String code, String description){
        com.db.awmd.challenge.exception.Error error = new Error(code, description);
        response.getErrors().add(error);
    }

    public static ResponseEntity getErrorResponse(Response response){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<Object>(response, headers, HttpStatus.BAD_REQUEST);
    }

    public static ResponseEntity getSuccessResponse(String message, HttpStatus status){
        return new ResponseEntity<>(message, status);
    }
}
