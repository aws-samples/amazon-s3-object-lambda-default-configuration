package com.example.s3objectlambda.request;

import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;

import java.net.URISyntaxException;
import java.util.Optional;

/**
 * This class implements UserRequest and represents "getObject" user request.
 */
public class GetObjectRequestWrapper extends UserRequestWrapper {

    private static final String RANGE = "Range";
    private static final String PART_NUMBER = "partNumber";

    public GetObjectRequestWrapper(S3ObjectLambdaEvent.UserRequest userRequest) {
        super(userRequest);
    }

    public Optional<String> getPartNumber() throws URISyntaxException {
        return this.getQueryParam(this.getUserRequest().getUrl(), PART_NUMBER);
    }

    public Optional<String> getRange() throws URISyntaxException {
        var range = this.getUserRequest().getHeaders().get(RANGE);

        if (range == null) {
            return this.getQueryParam(this.getUserRequest().getUrl(), RANGE);
        } else {
            return Optional.of(range);
        }
    }
}
