package com.amazon.s3objectlambda.request;

import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;

import java.util.Optional;

import static com.amazon.s3objectlambda.request.RequestUtil.getPartNumber;
import static com.amazon.s3objectlambda.request.RequestUtil.getRange;
/**
 * Responsible for validating the user request. Returns an Optional string error message if there are errors
 * or Optional empty if valid.
 */
public final class RequestValidator {

    private RequestValidator() {

    }

    public static Optional<String> validateUserRequest(S3ObjectLambdaEvent.UserRequest userRequest) {
        var range = getRange(userRequest);
        var partNumber = getPartNumber(userRequest);

        if (range != null && partNumber != null) {
            return Optional.of("Cannot specify both Range header and partNumber query parameter");
        }
        return Optional.empty();
    }
}
