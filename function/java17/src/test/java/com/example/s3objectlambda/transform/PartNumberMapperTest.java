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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class PartNumberMapperTest {

    private final String originalData = "12345678910!".repeat(300000);

    @Test
    @DisplayName("Get valid response for the final part number.")
    public void partNumberResponseObject()
            throws InvalidRangeException, URISyntaxException, InvalidPartNumberException {

        var responseInputStream = this.originalData.getBytes(StandardCharsets.UTF_16);
        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com?partNumber=2");

        /*Total responseInputStream byte[] length =  7200002 //(responseInputStream.length)
        partSize = 5242880 (PartNumberMapper::partSize)
        Total Parts = Math.ceil(7200002 / 5242880) = 2
        Final part (2nd) length = 7200002 - (1 *  5242880 ) = 1957122
        **/

        var totalParts = Math.ceil(responseInputStream.length / 5242880.0);
        var expectedPartLength = responseInputStream.length - ((totalParts - 1) * 5242880);
        var objectRequest = new GetObjectRequestWrapper(mockUserRequest);

        var transformedResponseObject = new GetObjectTransformer(objectRequest).applyRangeOrPartNumber(
                    responseInputStream);

        assertEquals((int) expectedPartLength, transformedResponseObject.length);
    }

    @Test
    @DisplayName("Get error response with correct status code when invalid part number is passed.")
    public void invalidPartNumberStatusCode() throws InvalidRangeException, URISyntaxException {

        var responseInputStream = this.originalData.getBytes(StandardCharsets.UTF_16);
        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com?partNumber=9");
        var objectRequest = new GetObjectRequestWrapper(mockUserRequest);

        try {
            new GetObjectTransformer(objectRequest).applyRangeOrPartNumber(
                    responseInputStream);
            fail("InvalidPartNumberException was not thrown.");
        } catch (InvalidPartNumberException e) {
            assertEquals(Error.INVALID_PART.getStatusCode(), e.getError().getStatusCode());
        }

    }
}
