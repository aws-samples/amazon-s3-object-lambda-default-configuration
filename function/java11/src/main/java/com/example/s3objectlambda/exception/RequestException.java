package com.example.s3objectlambda.exception;

import com.example.s3objectlambda.error.Error;

public abstract class RequestException extends Exception {

    private Error error;

    public RequestException(String errorMessage) {
        super(errorMessage);
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
