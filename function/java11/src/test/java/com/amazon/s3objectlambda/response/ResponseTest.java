package com.amazon.s3objectlambda.response;

import com.amazon.s3objectlambda.error.Error;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.amazon.s3objectlambda.response.ResponseUtil.getAttributeFromS3ErrorResponse;
import static com.amazon.s3objectlambda.response.ResponseUtil.getErrorResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResponseTest {

    @Test
    @DisplayName("Get error code from S3 xml error response")
    public void getErrorCodeFromS3ErrorResponse() {
        var s3ErrorResponse = getS3ErrorResponse();

        var errorCode = getAttributeFromS3ErrorResponse(s3ErrorResponse, "Code");
        assertEquals("NoSuchKey", errorCode);
    }

    @Test
    @DisplayName("Get error message from S3 xml error response")
    public void getErrorMessageFromS3ErrorResponse() {
        var s3ErrorResponse = getS3ErrorResponse();

        var errorMessage = getAttributeFromS3ErrorResponse(s3ErrorResponse, "Message");
        assertEquals("The resource you requested does not exist", errorMessage);
    }

    @Test
    @DisplayName("Get correct errorCode from getErrorResponse function")
    public void getErrorResponseErrorCode() {
        var errorResponse = getErrorResponse("Test Error Message", Error.INVALID_PART);
        assertEquals("InvalidPart", errorResponse.getErrorCode());
    }

    @Test
    @DisplayName("Get correct statusCode from getErrorResponse function")
    public void getErrorResponseStatusCode() {
        var errorResponse = getErrorResponse("Test Error Message", Error.INVALID_RANGE);
        var statusCode = errorResponse.getStatusCode();
        var expectedStatusCode = 416;
        assertEquals(expectedStatusCode, statusCode);
    }


    private String getS3ErrorResponse() {
        return "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Error>\n" +
                "  <Code>NoSuchKey</Code>\n" +
                "  <Message>The resource you requested does not exist</Message>\n" +
                "  <Resource>/mybucket/myfoto.jpg</Resource> \n" +
                "  <RequestId>4442587FB7D0A2F9</RequestId>\n" +
                "</Error>";
    }
}
