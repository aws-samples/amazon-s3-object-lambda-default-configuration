package com.example.s3objectlambda.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.MalformedURLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class S3PresignedUrlParserHelperTest {

    static final String TEST_URL_1 =
        "https://test-access-point-012345678901.s3-accesspoint.us-east-1.amazonaws.com/test" +
            "?X-Amz-Security-Token=TestToken" +
            "&X-Amz-Algorithm=AWS4-HMAC-SHA256" +
            "&X-Amz-Date=20250220T175710Z" +
            "&X-Amz-SignedHeaders=host%3Bx-amz-checksum-mode" +
            "&X-Amz-Expires=61" +
            "&X-Amz-Credential=AKIAEXAMPLE/20250220/us-east-1/s3/aws4_request" +
            "&X-Amz-Signature=a7f9b2c8e4d1f3a6b5c9e2d7a8f4b3c1d6e5f2a9b7c8d3e1f6a2b9c7d5e8f3a1";

    static final String TEST_URL_2 =
        "https://test-access-point-012345678901.s3-accesspoint.us-east-1.amazonaws.com/test" +
        "?X-Amz-Security-Token=TestToken" +
        "&X-Amz-Algorithm=AWS4-HMAC-SHA256" +
        "&X-Amz-Date=20250220T175710Z" +
        "&X-Amz-SignedHeaders=host" +
        "&X-Amz-Expires=61" +
        "&X-Amz-Credential=AKIAEXAMPLE/20250220/us-east-1/s3/aws4_request" +
        "&X-Amz-Signature=a7f9b2c8e4d1f3a6b5c9e2d7a8f4b3c1d6e5f2a9b7c8d3e1f6a2b9c7d5e8f3a1";

    static final String TEST_URL_3 =
        "https://test-access-point-012345678901.s3-accesspoint.us-east-1.amazonaws.com/test" +
        "?X-Amz-Security-Token=TestToken" +
        "&X-Amz-Algorithm=AWS4-HMAC-SHA256" +
        "&X-Amz-Date=20250220T175710Z" +
        "&X-Amz-Expires=61" +
        "&X-Amz-Credential=AKIAEXAMPLE/20250220/us-east-1/s3/aws4_request" +
        "&X-Amz-Signature=a7f9b2c8e4d1f3a6b5c9e2d7a8f4b3c1d6e5f2a9b7c8d3e1f6a2b9c7d5e8f3a1";

    static final String TEST_URL_MALFORMED =
        "htps://test-access-point-012345678901.s3-accesspoint.us-east-1.amazonaws.com/test" +
        "?X-Amz-Security-Token=TestToken" +
        "&X-Amz-Algorithm=AWS4-HMAC-SHA256" +
        "&X-Amz-Date=20250220T175710Z" +
        "&X-Amz-Expires=61" +
        "&X-Amz-Credential=AKIAEXAMPLE/20250220/us-east-1/s3/aws4_request" +
        "&X-Amz-Signature=a7f9b2c8e4d1f3a6b5c9e2d7a8f4b3c1d6e5f2a9b7c8d3e1f6a2b9c7d5e8f3a1";

    static final String HEADER_HOST = "host";
    static final String HEADER_CHECKSUM = "x-amz-checksum-mode";

    @Test
    @DisplayName("Parse URL with checksum and host signed headers.")
    void testParsePresignedUrlWithChecksumAndHost() throws MalformedURLException {
        List<String> signedHeaders = S3PresignedUrlParserHelper.retrieveSignedHeadersFromPresignedUrl(TEST_URL_1);
        assert signedHeaders != null;
        assertEquals(2, signedHeaders.size());
        assertEquals(HEADER_HOST, signedHeaders.get(0));
        assertEquals(HEADER_CHECKSUM, signedHeaders.get(1));
    }

    @Test
    @DisplayName("Parse URL with host signed header.")
    void testParsePresignedUrlWithHost() throws MalformedURLException {
        List<String> signedHeaders = S3PresignedUrlParserHelper.retrieveSignedHeadersFromPresignedUrl(TEST_URL_2);
        assert signedHeaders != null;
        assertEquals(1, signedHeaders.size());
        assertEquals(HEADER_HOST, signedHeaders.get(0));
    }

    @Test
    @DisplayName("Parse URL with no signed headers.")
    void testParsePresignedUrlWithNoSignedHeaders() throws MalformedURLException {
        List<String> signedHeaders = S3PresignedUrlParserHelper.retrieveSignedHeadersFromPresignedUrl(TEST_URL_3);
        assert signedHeaders != null;
        assertEquals(0, signedHeaders.size());
    }

    @Test
    @DisplayName("Try parse malformed URL.")
    void testParseMalformedPresignedUrl() {
        assertThrows(
            MalformedURLException.class,
            () -> S3PresignedUrlParserHelper.retrieveSignedHeadersFromPresignedUrl(TEST_URL_MALFORMED)
        );
    }
}
