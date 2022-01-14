package com.amazon.s3objectlambda.request;

import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;
import jdk.jfr.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static com.amazon.s3objectlambda.request.RequestUtil.getPartNumber;
import static com.amazon.s3objectlambda.request.RequestUtil.getRange;
import static com.amazon.s3objectlambda.request.RequestValidator.validateUserRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestTest {

    @Test
    @Description("The validateUserRequest should return error when both partNumber and Range (in the header) are given")
    public void userRequestWithPartNumberAndRange() {

        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com?pageNumber=3&partNumber=1");
        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=1-20");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);

        var requestValid = validateUserRequest(mockUserRequest);
        assertFalse(requestValid.isEmpty());
    }

    @Test
    @DisplayName("The validateUserRequest should return error when both partNumber and Range (query string) are given")
    public void userRequestWithPartNumberAndRangeInQueryString() {
        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com?pageNumber=3&partNumber=1&Range=bytes=-20");
        var requestValid = validateUserRequest(mockUserRequest);
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
        var requestValid = validateUserRequest(mockUserRequest);
        assertTrue(requestValid.isEmpty());
    }

    @Test
    @DisplayName("The validateUserRequest function should return empty when only partNumber is given in the request")
    public void validUserRequestWithPartNumber() {
        S3ObjectLambdaEvent.UserRequest mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com?partNumber=5");
        var requestValid = validateUserRequest(mockUserRequest);
        assertTrue(requestValid.isEmpty());
    }

    @Test
    @DisplayName("getRange function returns the range correctly.")
    public void getRangeTest() {
        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com?pageNumber=3&partNumber=1");
        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=1-20");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);

        var range = getRange(mockUserRequest);
        assertEquals("bytes=1-20", range);
    }

    @Test
    @DisplayName("getPartNumber function returns the range correctly.")
    public void getPartNumberTest() {
        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com?pageNumber=3&partNumber=4");
        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=1-20");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);

        var partNumber = getPartNumber(mockUserRequest);
        assertEquals("4", partNumber);
    }

}
