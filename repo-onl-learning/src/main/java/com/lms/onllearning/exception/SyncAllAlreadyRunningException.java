package com.lms.onllearning.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SyncAllAlreadyRunningException extends RuntimeException {
    public SyncAllAlreadyRunningException(String message) {
        super(message);
    }
}
