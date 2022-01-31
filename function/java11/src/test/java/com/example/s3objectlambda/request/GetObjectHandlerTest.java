package com.example.s3objectlambda.request;

import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.util.IOUtils;
import com.example.s3objectlambda.checksum.Md5Checksum;
import com.example.s3objectlambda.error.XMLErrorParser;
import com.example.s3objectlambda.response.GetObjectResponseHandler;
import com.example.s3objectlambda.transform.GetObjectTransformer;
import com.example.s3objectlambda.validator.GetObjectRequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.any;


/**
 *  This class perform unit test on the main GetObjectHandler.
 *  This test try to mock the s3 getObject behaviour by
 *  reading a mock s3 object (File: /src/test/resources/mock_s3_objects/mock_s3_object.txt)
 */

@ExtendWith(MockitoExtension.class)
public class GetObjectHandlerTest {
    @Mock
    private Logger logger;
    @Mock
    private AmazonS3 s3Client;
    @Mock
    private GetObjectTransformer transformer;
    @Mock
    private GetObjectRequestValidator requestValidator;
    @Mock
    private GetObjectResponseHandler responseHandler;
    @Mock
    private S3ObjectLambdaEvent s3ObjectLambdaEvent;
    @Mock
    private HttpClient httpClient;

    @InjectMocks
    private GetObjectHandler underTest;


    @Nested
    class WhenHandlingGetObjectRequests {
        @BeforeEach
        void setup() throws IOException, InterruptedException {

            // Mock S3ObjectLambdaEvent
            // You can mock the details of the request here such as Range and partNumber.
            mockS3ObjectLambdaEvent();

            // Mock getObjectResponse
            // This function mocks the original getObject S3 response including the http code.
            // If you want different responses for each test, call this function from the respective tests.
            mockHttpResponseFromS3(200,
                    "src/test/resources/mock_s3_objects/mock_s3_object.txt");

            var userRequest = new GetObjectRequestWrapper(s3ObjectLambdaEvent.getUserRequest());

            //Mock Validator
            requestValidator =  Mockito.spy(new GetObjectRequestValidator(userRequest));

            //Mock Transformer
            transformer =  Mockito.spy(new GetObjectTransformer(userRequest));

            //Mock Checksum
            var md5Checksum =  Mockito.spy(new Md5Checksum());

            //Mock Response Handler
            responseHandler = Mockito.spy(new GetObjectResponseHandler(s3Client, s3ObjectLambdaEvent, md5Checksum));
        }

        @Test
        void testHandleRequestTransformObject() {
            var getObjectHandler = new GetObjectHandler(s3Client, transformer, requestValidator, s3ObjectLambdaEvent,
                    responseHandler, httpClient);

            // Response has already been mocked in the setup function.

            // Capture the second argument when the handler calls writeObjectResponse.
            // Second argument is the responseObjectArray after applying range and transformation.
            ArgumentCaptor<byte[]> responseObjectArray = ArgumentCaptor.forClass(byte[].class);
            lenient().doNothing().when(responseHandler).writeObjectResponse(any(), responseObjectArray.capture());

            getObjectHandler.handleRequest();

            //We applied range and transformation on original the mock S3 Object.
            // (/src/test/resources/mock_s3_objects/mock_s3_object.txt)

            var expectedBody = "What is Amazon S3?";
            var transformedResponse = new String(responseObjectArray.getValue(), StandardCharsets.UTF_8);
            assertEquals(expectedBody, transformedResponse);

        }

        @Test
        @DisplayName("Correct error reaches writeS3GetObjectErrorResponse when s3 getObject returns >= 4**")
        void testHandleRequestWith400S3Error() throws IOException, InterruptedException,
                ParserConfigurationException, SAXException {

            var getObjectHandler = new GetObjectHandler(s3Client, transformer, requestValidator, s3ObjectLambdaEvent,
                    responseHandler, httpClient);

            mockHttpResponseFromS3(404,
                    "src/test/resources/mock_responses/mock_s3_error_response.txt");

            ArgumentCaptor<HttpResponse<InputStream>> presignedResponse = ArgumentCaptor.forClass(HttpResponse.class);
            lenient().doNothing().when(responseHandler).writeS3GetObjectErrorResponse(presignedResponse.capture());
            getObjectHandler.handleRequest();

            //This will be our mock error response. (/src/test/resources/mock_s3_error_response.txt)
            var xmlResponse = IOUtils.toString(presignedResponse.getValue().body());
            var s3errorResponse = new XMLErrorParser().parse(xmlResponse);
            assertEquals("NoSuchMockKey", s3errorResponse.getCode());

        }
    }

    /**
     *
     * @param httpStatusCode Http status code of the mock response
     * @param mockS3ObjectFilePath File path of the mock s3 object. Http response will be the content of this file.
     * @throws IOException
     * @throws InterruptedException
     */
    private void mockHttpResponseFromS3(int httpStatusCode, String mockS3ObjectFilePath) throws IOException,
            InterruptedException {
        var httpResponse = mock(HttpResponse.class);
        lenient().when(httpClient.send(any(HttpRequest.class),
                any())).thenReturn(httpResponse);
        lenient().when(httpResponse.statusCode()).thenReturn(httpStatusCode);
        var responseBody = getFileInputStream(mockS3ObjectFilePath);
        lenient().when(httpResponse.body()).thenReturn(responseBody);
    }

    private InputStream getFileInputStream(String mockS3ObjectFilePath) throws IOException {
            File initialFile = new File(mockS3ObjectFilePath);
            InputStream targetStream = new FileInputStream(initialFile);
        return targetStream;
    }

    /**
     * Mocks the user S3ObjectLambdaEvent.
     * This contains the user request, which has Range and partNumber.
     */
    private void mockS3ObjectLambdaEvent() {
        var mockUserRequest = mock(S3ObjectLambdaEvent.UserRequest.class);
        lenient().when(mockUserRequest.getUrl()).thenReturn("https://example.com?time=great!");
        var headerMap = new HashMap<String, String>();
        headerMap.put("Range", "bytes=0-17");
        lenient().when(mockUserRequest.getHeaders()).thenReturn(headerMap);
        lenient().when(s3ObjectLambdaEvent.getUserRequest()).thenReturn(mockUserRequest);
        lenient().when(s3ObjectLambdaEvent.inputS3Url()).thenReturn("https://aws-region.example.com/getObject.fakeurl");
    }
}
