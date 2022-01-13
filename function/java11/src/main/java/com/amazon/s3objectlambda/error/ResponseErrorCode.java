package com.amazon.s3objectlambda.error;

import java.util.EnumMap;

/**
 * The EnumMaps properties  errorCode and statusCode are used to map the
 * Error with appropriate http status code and s3 error codes.
 */

public class ResponseErrorCode {
    private final EnumMap<Error, String> errorCode = new EnumMap<Error, String>(Error.class);
    private final EnumMap<Error, Integer> statusCode = new EnumMap<Error, Integer>(Error.class);

    public ResponseErrorCode() {
        this.errorCode.put(Error.INVALID_REQUEST, "InvalidRequest");
        this.errorCode.put(Error.INVALID_RANGE, "InvalidRange");
        this.errorCode.put(Error.INVALID_PART, "InvalidPart");
        this.errorCode.put(Error.NO_SUCH_KEY, "NoSuchKey");
        this.errorCode.put(Error.SERVER_ERROR, "LambdaRuntimeError");

        this.statusCode.put(Error.INVALID_REQUEST, 400);
        this.statusCode.put(Error.INVALID_RANGE, 416);
        this.statusCode.put(Error.INVALID_PART, 400);
        this.statusCode.put(Error.NO_SUCH_KEY, 404);
        this.statusCode.put(Error.SERVER_ERROR, 500);
    }

    public EnumMap<Error, Integer> getStatusCode() {
        return this.statusCode;
    }

    public EnumMap<Error, String> getErrorCode() {
        return this.errorCode;
    }
}
