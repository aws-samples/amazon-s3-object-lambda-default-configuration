package com.example.s3objectlambda.transform;

import com.example.s3objectlambda.exception.InvalidPartNumberException;
import com.example.s3objectlambda.exception.InvalidRangeException;
import com.example.s3objectlambda.exception.TransformationException;
import com.example.s3objectlambda.request.GetObjectRequestWrapper;
import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransformTest {

    private static final String ORIGINAL_RESPONSE = "12345678910!";

    @Test
    @DisplayName("TODO: Transform logic works as expected.")
    public void transformObjectResponseTest() throws TransformationException {
        //Todo: Rewrite this test based your transformation logic.

        byte[] responseInputStream = ORIGINAL_RESPONSE.getBytes(StandardCharsets.UTF_16);
        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        var objectRequest = new GetObjectRequestWrapper(mockUserRequest);
        var transformedResponseObject = new GetObjectTransformer(objectRequest)
                .transformObjectResponse(responseInputStream);
        var transformedString = new String(transformedResponseObject, StandardCharsets.UTF_16);
        assertEquals(ORIGINAL_RESPONSE, transformedString);
    }

    @Test
    @DisplayName("Apply Range or partNumber on object and verify the size.")
    public void applyRangeOrPartNumberHasError()
            throws InvalidRangeException, URISyntaxException, InvalidPartNumberException {
        var responseInputStream = ORIGINAL_RESPONSE.getBytes(StandardCharsets.UTF_16);

        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com?Range=bytes=1-3");

        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=1-3");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);
        var objectRequest = new GetObjectRequestWrapper(mockUserRequest);
        var transformedResponseObject = new GetObjectTransformer(objectRequest).applyRangeOrPartNumber(
                responseInputStream);

        assertEquals(3, transformedResponseObject.length);

    }

    @Test
    @DisplayName("Apply Range or partNumber on object and verify response")
    public void applyRangeOrPartNumberResponseObject()
            throws InvalidRangeException, URISyntaxException, InvalidPartNumberException {
        byte[] responseInputStream = ORIGINAL_RESPONSE.getBytes(StandardCharsets.UTF_16);

        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com");

        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=-26");

        when(mockUserRequest.getHeaders()).thenReturn(headerMap);
        var objectRequest = new GetObjectRequestWrapper(mockUserRequest);
        var transformedResponseObject = new GetObjectTransformer(objectRequest)
                .applyRangeOrPartNumber(responseInputStream);

        var transformedString = new String(transformedResponseObject, StandardCharsets.UTF_16);
        assertEquals(ORIGINAL_RESPONSE, transformedString);
    }
}
