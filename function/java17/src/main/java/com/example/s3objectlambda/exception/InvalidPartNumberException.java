package com.example.s3objectlambda.exception;

import com.example.s3objectlambda.error.Error;

public class InvalidPartNumberException extends RequestException {
    public InvalidPartNumberException(String message) {
        super(message);
        this.setError(Error.INVALID_PART);
    }
}
