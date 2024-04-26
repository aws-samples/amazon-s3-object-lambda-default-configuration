package com.example.s3objectlambda.exception;

import com.example.s3objectlambda.error.Error;

/**
 *  This exception class represents any exception related to, or that occurs during the transformation of the object.
 */
public class TransformationException extends RequestException {
    public TransformationException(String message) {
        super(message);
        this.setError(Error.SERVER_ERROR);
    }

    public TransformationException(String message, Error error) {
        super(message);
        this.setError(error);
    }
}
