package com.example.s3objectlambda.request;

/**
 * This interface should be implemented by the class that handles the user request.
 */
public interface RequestHandler {
    void handleRequest() throws Exception;
}
