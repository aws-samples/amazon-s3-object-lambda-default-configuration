package com.amazon.s3objectlambda.transform;

import java.util.Arrays;

/**
 * This defines the structure for a transformed object and has setter and getter methods.
 */
public class TransformedObject {

    /**
     * This is set to true if the response object has any error.
     * This could be an error while transforming the object.
     */
    private boolean hasError;

    /**
     * The error code is a string that uniquely identifies an error condition.
     * https://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html#ErrorCodeList
     */
    private String errorCode;

    /**
     * Http Status code for the object response.
     */
    private Integer statusCode;

    /**
     * Actual response body.
     */
    private byte[] objectResponse;

    private String errorMessage;

    public boolean getHasError() {
        return this.hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Integer getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public byte[] getObjectResponse() {
        return Arrays.copyOf(this.objectResponse, this.objectResponse.length);
    }

    public void setObjectResponse(byte[] objectResponse) {
        this.objectResponse = Arrays.copyOf(objectResponse, objectResponse.length);
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
