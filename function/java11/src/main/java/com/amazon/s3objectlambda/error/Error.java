package com.amazon.s3objectlambda.error;

/**
 * The list of error codes returned from the Lambda function. We use the same error codes that are
 * supported by Amazon S3, where possible.
 * @see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html#ErrorCodeList">List of Error Codes</a>
 * for more information.
 */
public enum Error {
    INVALID_REQUEST,
    INVALID_RANGE,
    INVALID_PART,
    NO_SUCH_KEY,
    SERVER_ERROR,
}
