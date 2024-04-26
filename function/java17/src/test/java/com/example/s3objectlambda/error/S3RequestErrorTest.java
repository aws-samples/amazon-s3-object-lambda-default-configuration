package com.example.s3objectlambda.error;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class S3RequestErrorTest {
    private XMLErrorParser xmlErrorParser;
    private S3RequestError s3RequestError;

    @BeforeAll
    void setup() throws ParserConfigurationException, IOException, SAXException {
        this.xmlErrorParser = new XMLErrorParser();
        var xmlResponse = getS3XMLErrorResponse();
        this.s3RequestError = this.xmlErrorParser.parse(xmlResponse);
    }

    @Test
    void testGetCode() {
        assertEquals("No-SuchKey", this.s3RequestError.getCode());
    }

    @Test
    void testGetMessage() {
        assertEquals("The resource you requested does not exist", this.s3RequestError.getMessage());
    }

    @Test
    void testGetRequestId() {
        assertEquals("4442587FB7D0A2F9", this.s3RequestError.getRequestId());
    }

    @Test
    @DisplayName("Parser Throws Exception on invalid XMl.")
    void testParseTrowsException() {
        try {
            this.xmlErrorParser.parse("<InvalidXML></InvalidXNK>");
            fail("Parser did not throw exception.");
        } catch (ParserConfigurationException | SAXException | IOException e) {
            assertTrue(!e.getMessage().isEmpty());
        }
    }

    @Test
    @DisplayName("Parser Throws Exception on valid XML but unexpected response.")
    void testParseTrowsExceptionInvalidStructure() {
        try {
            var xmlResponse = this.getS3XMLInvalidErrorResponse();
            this.xmlErrorParser.parse(xmlResponse);
            fail("Parser did not throw exception.");
        } catch (ParserConfigurationException | SAXException | IOException  | NullPointerException e) {

        }
    }

    @Test
    @DisplayName("Parser Throws Exception on valid XML but missing Code.")
    void testParseTrowsExceptionInvalidStructureMissingCode() {
        try {
            var xmlResponse = this.getS3XMLInvalidErrorResponseNoCode();
            this.xmlErrorParser.parse(xmlResponse);
            fail("Parser did not throw exception.");
        } catch (ParserConfigurationException | SAXException | IOException  | NullPointerException e) {

        }
    }

    private String getS3XMLErrorResponse() {
        return "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Error>\n" +
                "  <Code>No-SuchKey</Code>\n" +
                "  <Message>The resource you requested does not exist</Message>\n" +
                "  <Resource>/mybucket/myfoto.jpg</Resource> \n" +
                "  <RequestId>4442587FB7D0A2F9</RequestId>\n" +
                "</Error>";
    }

    private String getS3XMLInvalidErrorResponse() {
        return "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Error>\n" +
                "  <OKCode>No-SuchKey</OKCode>\n" +
                "  <Message>The resource you requested does not exist</Message>\n" +
                "  <Resource>/mybucket/myfoto.jpg</Resource> \n" +
                "  <RequestId>4442587FB7D0A2F9</RequestId>\n" +
                "</Error>";
    }

    private String getS3XMLInvalidErrorResponseNoCode() {
        return "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Error>\n" +
                "  <Message>The resource you requested does not exist</Message>\n" +
                "  <Resource>/mybucket/myfoto.jpg</Resource> \n" +
                "  <RequestId>4442587FB7D0A2F9</RequestId>\n" +
                "</Error>";
    }
}
