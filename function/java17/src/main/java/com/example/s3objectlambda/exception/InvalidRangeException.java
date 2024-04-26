package com.example.s3objectlambda.exception;

import com.example.s3objectlambda.error.Error;

public class InvalidRangeException extends RequestException {

    public InvalidRangeException(String message) {
        super(message);
        this.setError(Error.INVALID_RANGE);
    }
}
