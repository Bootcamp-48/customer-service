package com.bootcamp.customerservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCustomerTypeException extends RuntimeException {

    public InvalidCustomerTypeException(String message) {
        super(message);
    }


}