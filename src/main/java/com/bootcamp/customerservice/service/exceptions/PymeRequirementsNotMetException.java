package com.bootcamp.customerservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PymeRequirementsNotMetException extends RuntimeException{

    public PymeRequirementsNotMetException(String message) {
        super(message);
    }
}