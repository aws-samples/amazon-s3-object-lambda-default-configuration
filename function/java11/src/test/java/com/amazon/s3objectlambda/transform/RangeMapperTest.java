package com.amazon.s3objectlambda.transform;

import com.amazon.s3objectlambda.error.Error;
import com.amazon.s3objectlambda.error.ResponseErrorCode;
import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static com.amazon.s3objectlambda.transform.ResponseTransformer.applyRangeOrPartNumber;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RangeMapperTest {


    @Test
    @DisplayName("Appropriate statusCode is set in the response when invalid range is passed ")
    public void applyRangeOrPartNumberResponseObject() {
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

        var transformedResponseObject = applyRangeOrPartNumber(
                responseInputStream, mockUserRequest);
        var invalidRangeStatusCode = 416;
        assertEquals(invalidRangeStatusCode, transformedResponseObject.getStatusCode());

    }

    @Test
    @DisplayName("The function returns error response when invalid range passed: bytes=-")
    public void applyRangeOrPartNumberResponseObjectInvalidRange() {
        var stringOriginalResponse = "12345678910!";
        var responseInputStream = stringOriginalResponse.getBytes(StandardCharsets.UTF_16);

        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com");

        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=-");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);

        var transformedResponseObject = applyRangeOrPartNumber(
                responseInputStream, mockUserRequest);
        var invalidRangeStatusCode = 416;
        assertEquals(invalidRangeStatusCode, transformedResponseObject.getStatusCode());

    }

    @Test
    @DisplayName("Valid response when correct range is passed: bytes=2-5")
    public void applyRangeOrPartNumberResponseObjectValidRange() {
        var stringOriginalResponse = "12345678910!12345678910!";
        var responseInputStream = stringOriginalResponse.getBytes(StandardCharsets.UTF_16);

        S3ObjectLambdaEvent.UserRequest mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com");

        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=2-5");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);

        var transformedResponseObject = applyRangeOrPartNumber(
                responseInputStream, mockUserRequest);
        var transformedString = new String(transformedResponseObject.getObjectResponse(), StandardCharsets.UTF_16);

        assertEquals("12", transformedString);

    }

    @Test
    @DisplayName("Valid response when correct range is passed: bytes=6-")
    public void applyRangeOrPartNumberResponseObjectValidRangeFirstPart() {
        var stringOriginalResponse = "12345678910!12345678910!";
        var responseInputStream = stringOriginalResponse.getBytes(StandardCharsets.UTF_16);

        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com");

        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=6-");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);

        var transformedResponseObject = applyRangeOrPartNumber(
                responseInputStream, mockUserRequest);
        var transformedString = new String(transformedResponseObject.getObjectResponse(), StandardCharsets.UTF_16);

        assertEquals("345678910!12345678910!", transformedString);

    }

    @Test
    @DisplayName("Valid response when correct range is passed: bytes=-12")
    public void applyRangeOrPartNumberResponseObjectValidRangeSuffixLength() {
        var stringOriginalResponse = "S3 Object Lambda";
        byte[] responseInputStream = stringOriginalResponse.getBytes(StandardCharsets.UTF_16);

        S3ObjectLambdaEvent.UserRequest mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com");

        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=-12");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);

        var transformedResponseObject = applyRangeOrPartNumber(
                responseInputStream, mockUserRequest);
        var transformedString = new String(transformedResponseObject.getObjectResponse(), StandardCharsets.UTF_16);

        assertEquals("Lambda", transformedString);

    }

    @Test
    @DisplayName("Error response when invalid unit is passed in range")
    public void applyRangeOrPartNumberResponseObjectInvalidUnit() {
        var stringOriginalResponse = "S3 Object Lambda";
        var responseInputStream = stringOriginalResponse.getBytes(StandardCharsets.UTF_16);

        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com");

        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bits=-12");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);

        var transformedResponseObject = applyRangeOrPartNumber(
                responseInputStream, mockUserRequest);

        assertEquals(new ResponseErrorCode().getErrorCode().get(Error.INVALID_RANGE),
                transformedResponseObject.getErrorCode());

    }

}
