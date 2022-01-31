package com.example.s3objectlambda.error;

/**
 * This class represents the S3 Error response.
 * Each error attribute can be accessed using the get method.
 *
 * @see https://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html#RESTErrorResponses
 */

public class S3RequestError {

    private String code;
    private String message;
    private String requestId;

    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public String getRequestId() {
        return this.requestId;
    }

}
