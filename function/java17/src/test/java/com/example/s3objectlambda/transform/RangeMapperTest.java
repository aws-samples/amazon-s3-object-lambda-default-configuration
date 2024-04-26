package com.example.s3objectlambda.transform;

import com.example.s3objectlambda.error.Error;
import com.example.s3objectlambda.exception.InvalidPartNumberException;
import com.example.s3objectlambda.exception.InvalidRangeException;
import com.example.s3objectlambda.request.GetObjectRequestWrapper;
import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RangeMapperTest {

    @Test
    @DisplayName("InvalidRangeException thrown with right statusCode. Edge case: bytes=-27")
    public void applyRangeOrPartNumberResponseObject() throws URISyntaxException, InvalidPartNumberException {
        var stringOriginalResponse = "12345678910!";
        var responseInputStream = stringOriginalResponse.getBytes(StandardCharsets.UTF_16);

        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com");

        var headerMap = new HashMap<String, String>();
        /*
        Total byte length of the original response (responseInputStream) is 26
        Range mapper should return an error object with correct status code.
        Status code for invalid range is 416 (Range Not Satisfiable).
        https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/416
        */
        headerMap.put("Range", "bytes=-27");

        when(mockUserRequest.getHeaders()).thenReturn(headerMap);
        var objectRequest = new GetObjectRequestWrapper(mockUserRequest);
        try {
            new GetObjectTransformer(objectRequest).applyRangeOrPartNumber(
                    responseInputStream);
            fail("Did not throw InvalidRangeException");
        } catch (InvalidRangeException e) {
            var invalidRangeStatusCode = 416;
            assertEquals(invalidRangeStatusCode, e.getError().getStatusCode());
        }
    }

    @Test
    @DisplayName("InvalidRangeException thrown and appropriate statusCode is set: bytes=-")
    public void applyRangeOrPartNumberResponseObjectInvalidRange()
            throws URISyntaxException, InvalidPartNumberException {
        var stringOriginalResponse = "12345678910!";
        var responseInputStream = stringOriginalResponse.getBytes(StandardCharsets.UTF_16);

        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com");

        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=-");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);
        var objectRequest = new GetObjectRequestWrapper(mockUserRequest);

        try {
            new GetObjectTransformer(objectRequest).applyRangeOrPartNumber(
                    responseInputStream);
        } catch (InvalidRangeException e) {
            var invalidRangeStatusCode = 416;
            assertEquals(invalidRangeStatusCode, e.getError().getStatusCode());
        }
    }

    @Test
    @DisplayName("Valid response when correct range is passed: bytes=2-5")
    public void applyRangeOrPartNumberResponseObjectValidRange()
            throws InvalidRangeException, URISyntaxException, InvalidPartNumberException {
        var stringOriginalResponse = "12345678910!12345678910!";
        var responseInputStream = stringOriginalResponse.getBytes(StandardCharsets.UTF_16);

        S3ObjectLambdaEvent.UserRequest mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com");

        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=2-5");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);
        var objectRequest = new GetObjectRequestWrapper(mockUserRequest);
        var transformedResponseObject = new GetObjectTransformer(objectRequest).applyRangeOrPartNumber(
                responseInputStream);
        var transformedString = new String(transformedResponseObject, StandardCharsets.UTF_16);

        assertEquals("12", transformedString);

    }

    @Test
    @DisplayName("Valid response when correct range is passed: bytes=6-")
    public void applyRangeOrPartNumberResponseObjectValidRangeFirstPart()
            throws InvalidRangeException, URISyntaxException, InvalidPartNumberException {
        var stringOriginalResponse = "12345678910!12345678910!";
        var responseInputStream = stringOriginalResponse.getBytes(StandardCharsets.UTF_16);

        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com");

        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=6-");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);
        var objectRequest = new GetObjectRequestWrapper(mockUserRequest);
        var transformedResponseObject = new GetObjectTransformer(objectRequest).applyRangeOrPartNumber(
                responseInputStream);
        var transformedString = new String(transformedResponseObject, StandardCharsets.UTF_16);

        assertEquals("345678910!12345678910!", transformedString);

    }

    @Test
    @DisplayName("Valid response when correct range is passed: bytes=-12")
    public void applyRangeOrPartNumberResponseObjectValidRangeSuffixLength()
            throws InvalidRangeException, URISyntaxException, InvalidPartNumberException {
        var stringOriginalResponse = "S3 Object Lambda";
        byte[] responseInputStream = stringOriginalResponse.getBytes(StandardCharsets.UTF_16);

        S3ObjectLambdaEvent.UserRequest mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com");

        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=-12");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);
        var objectRequest = new GetObjectRequestWrapper(mockUserRequest);
        var transformedResponseObject = new GetObjectTransformer(objectRequest).applyRangeOrPartNumber(
                responseInputStream);
        var transformedString = new String(transformedResponseObject, StandardCharsets.UTF_16);

        assertEquals("Lambda", transformedString);

    }

    @Test
    @DisplayName("Error response when invalid unit is passed in range :bits=-12")
    public void applyRangeOrPartNumberResponseObjectInvalidUnit()
            throws URISyntaxException, InvalidPartNumberException {
        var stringOriginalResponse = "S3 Object Lambda";
        var responseInputStream = stringOriginalResponse.getBytes(StandardCharsets.UTF_16);

        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com");

        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bits=-12");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);
        var objectRequest = new GetObjectRequestWrapper(mockUserRequest);

        try {
            new GetObjectTransformer(objectRequest).applyRangeOrPartNumber(
                    responseInputStream);
        } catch (InvalidRangeException e) {
            assertEquals(Error.INVALID_RANGE.getErrorCode(), e.getError().getErrorCode());
        }



    }

}
