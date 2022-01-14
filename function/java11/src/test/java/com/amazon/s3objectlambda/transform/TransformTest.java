package com.amazon.s3objectlambda.transform;

import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.amazon.s3objectlambda.transform.ResponseTransformer.applyRangeOrPartNumber;
import static com.amazon.s3objectlambda.transform.ResponseTransformer.transformObjectResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransformTest {

    private static String stringOriginalResponse = "12345678910!";

    @Test
    @DisplayName("Transform object response. Test the object transformation here.")
    public void transformObjectResponseTest() {
        byte[] responseInputStream = stringOriginalResponse.getBytes(StandardCharsets.UTF_16);

        var transformedResponseObject = transformObjectResponse(responseInputStream);
        var transformedString = new String(transformedResponseObject.getObjectResponse(), StandardCharsets.UTF_16);
        assertEquals(stringOriginalResponse, transformedString);

    }

    @Test
    @DisplayName("hasError flag is set correctly for transform object.")
    public void transformObjectResponseHasError() {
        byte[] responseInputStream = stringOriginalResponse.getBytes(StandardCharsets.UTF_16);

        var transformedResponseObject = transformObjectResponse(responseInputStream);
        assertEquals(false, transformedResponseObject.getHasError());

    }

    @Test
    @DisplayName("Apply Range or partNumber on object and verify hasError flag on the object")
    public void applyRangeOrPartNumberHasError() {
        var responseInputStream = stringOriginalResponse.getBytes(StandardCharsets.UTF_16);

        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com?Range=bytes=1-3");

        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=1-3");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);

        var transformedResponseObject = applyRangeOrPartNumber(
                responseInputStream, mockUserRequest);

        assertEquals(false, transformedResponseObject.getHasError());

    }

    @Test
    @DisplayName("Apply Range or partNumber on object and verify response")
    public void applyRangeOrPartNumberResponseObject() {
        byte[] responseInputStream = stringOriginalResponse.getBytes(StandardCharsets.UTF_16);

        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com");

        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=-26");

        when(mockUserRequest.getHeaders()).thenReturn(headerMap);

        var transformedResponseObject = applyRangeOrPartNumber(
                responseInputStream, mockUserRequest);

        var transformedString = new String(transformedResponseObject.getObjectResponse(), StandardCharsets.UTF_16);
        assertEquals(stringOriginalResponse, transformedString);
    }

}
