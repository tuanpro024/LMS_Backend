package com.lms.onllearning.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SyncAllJobNotFoundException extends RuntimeException {
    public SyncAllJobNotFoundException(String message) {
        super(message);
    }
}
