package com.example.s3objectlambda.request;

import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetObjectRequestTest {

    @Test
    @DisplayName("getRange function returns the range correctly.")
    public void getRangeTest() throws URISyntaxException {
        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com?pageNumber=3&partNumber=1");
        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=1-20");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);
        var getObjectRequest = new GetObjectRequestWrapper(mockUserRequest);
        var range = getObjectRequest.getRange().get();
        assertEquals("bytes=1-20", range);
    }

    @Test
    @DisplayName("getPartNumber function returns the part correctly.")
    public void getPartNumberTest() throws URISyntaxException {
        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        when(mockUserRequest.getUrl()).thenReturn("https://example.com?pageNumber=3&partNumber=4");
        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=1-20");
        when(mockUserRequest.getHeaders()).thenReturn(headerMap);
        var getObjectRequest = new GetObjectRequestWrapper(mockUserRequest);
        var partNumber = getObjectRequest.getPartNumber().get();
        assertEquals("4", partNumber);
    }

}
