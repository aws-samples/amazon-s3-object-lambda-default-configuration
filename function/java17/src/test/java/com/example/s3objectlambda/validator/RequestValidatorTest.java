package com.example.s3objectlambda.validator;

import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;
import com.example.s3objectlambda.request.GetObjectRequestWrapper;
import jdk.jfr.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestValidatorTest {
    @Test
    @Description("The validateUserRequest should return error when both partNumber and Range (in the header) are given")
    public void userRequestWithPartNumberAndRange() {

        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com?pageNumber=3&partNumber=1");
        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=1-20");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);
        var getObjectUserRequest = new GetObjectRequestWrapper(mockUserRequest);
        var requestValid = new GetObjectRequestValidator(getObjectUserRequest).validateUserRequest();
        assertFalse(requestValid.isEmpty());
    }

    @Test
    @DisplayName("The validateUserRequest should return error when both partNumber and Range (query string) are given")
    public void userRequestWithPartNumberAndRangeInQueryString() {
        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com?pageNumber=3&partNumber=1&Range=bytes=-20");
        var getObjectUserRequest = new GetObjectRequestWrapper(mockUserRequest);
        var requestValid = new GetObjectRequestValidator(getObjectUserRequest).validateUserRequest();
        assertFalse(requestValid.isEmpty());
    }

    @Test
    @DisplayName("The validateUserRequest function should return empty when " +
            "only Range (in the query string and in header) is given")
    public void validUserRequestWithRange() {
        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com?Range=bytes=-20");

        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=1-20");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);
        var getObjectUserRequest = new GetObjectRequestWrapper(mockUserRequest);
        var requestValid = new GetObjectRequestValidator(getObjectUserRequest).validateUserRequest();
        assertTrue(requestValid.isEmpty());
    }

    @Test
    @DisplayName("The validateUserRequest function should return empty when only partNumber is given in the request")
    public void validUserRequestWithPartNumber() {
        S3ObjectLambdaEvent.UserRequest mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        var getObjectUserRequest = new GetObjectRequestWrapper(mockUserRequest);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com?partNumber=5");
        var requestValid = new GetObjectRequestValidator(getObjectUserRequest).validateUserRequest();
        assertTrue(requestValid.isEmpty());
    }
}
