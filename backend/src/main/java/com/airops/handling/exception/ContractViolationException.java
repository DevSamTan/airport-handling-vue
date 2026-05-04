package com.airops.handling.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ContractViolationException extends RuntimeException {
    public ContractViolationException(String message) {
        super(message);
    }
}
