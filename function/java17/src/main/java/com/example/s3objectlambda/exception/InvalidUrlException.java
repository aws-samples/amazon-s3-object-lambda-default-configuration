package com.example.s3objectlambda.exception;

import com.example.s3objectlambda.error.Error;

public class InvalidUrlException extends RequestException {
    public InvalidUrlException(String message) {
        super(message);
        this.setError(Error.INVALID_URL);
    }
}
