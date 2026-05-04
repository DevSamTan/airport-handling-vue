package com.airops.handling.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ShiftConflictException extends RuntimeException {
    public ShiftConflictException(String staffName, String date) {
        super("Shift already assigned for " + staffName + " on " + date);
    }
}
