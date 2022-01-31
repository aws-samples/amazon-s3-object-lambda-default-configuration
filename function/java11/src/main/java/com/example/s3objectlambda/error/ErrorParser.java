package com.example.s3objectlambda.error;

/**
 * This interface should be implemented by any parser that parses the error response from S3 getObject request.
 */
public interface ErrorParser {
    S3RequestError parse(String errorResponse) throws Exception;
}
