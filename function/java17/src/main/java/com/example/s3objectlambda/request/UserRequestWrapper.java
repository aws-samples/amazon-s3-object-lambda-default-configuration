package com.example.s3objectlambda.request;

import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * This class is a wrapper over original Lambda event UserRequest.
 */
public class UserRequestWrapper {
    public void setUserRequest(S3ObjectLambdaEvent.UserRequest userRequest) {
        this.userRequest = userRequest;
    }

    private S3ObjectLambdaEvent.UserRequest userRequest;

    public UserRequestWrapper(S3ObjectLambdaEvent.UserRequest userRequest) {
        this.userRequest = userRequest;
    }
    public S3ObjectLambdaEvent.UserRequest getUserRequest() {
        return this.userRequest;
    }

    public Optional<String> getQueryParam(String url, String partNumber) throws URISyntaxException {
            var params = URLEncodedUtils.parse(new URI(url), "UTF-8");
            for (NameValuePair param : params) {
                if (param.getName().equals(partNumber)) {
                    return Optional.of(param.getValue());
                }
            }
            return Optional.empty();
    }


}
