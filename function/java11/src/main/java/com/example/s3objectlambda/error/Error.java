package com.example.s3objectlambda.error;

/**
 * The list of error codes returned from the Lambda function. We use the same error codes that are
 * supported by Amazon S3, where possible.
 *
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html#ErrorCodeList">List of Error Codes</a>
 * for more information.
 *
 */
public enum Error {

    INVALID_REQUEST(400, "InvalidRequest"),
    INVALID_RANGE(416, "InvalidRange"),
    INVALID_PART(400, "InvalidPart"),
    NO_SUCH_KEY(404, "NoSuchKey"),
    SERVER_ERROR(500, "LambdaRuntimeError");

    private final Integer statusCode;
    private final String errorCode;

    Error(Integer statusCode, String errorCode) {
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public Integer getStatusCode() {
        return this.statusCode;
    }

}
