package com.amazon.s3objectlambda.response;

import com.amazon.s3objectlambda.error.Error;
import com.amazon.s3objectlambda.error.ResponseErrorCode;
import com.amazon.s3objectlambda.transform.TransformedObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

public final class ResponseUtil {

    private ResponseUtil() {

    }

    /**
     * Returns a specific attribute's value from AWS S3 error response
     * @param errorResponse xml error response from S3 getObject call.
     * @param attribute The specific attribute of the error(Code, Message, Resource etc.), whose value to be returned.
     * @return String value of attribute.
     */
    public static String getAttributeFromS3ErrorResponse(String errorResponse, String attribute) {
        var factory = DocumentBuilderFactory.newInstance();
        /*
        Prevent XML External Entity (XXE) Processing
        https://owasp.org/www-community/vulnerabilities/XML_External_Entity_(XXE)_Processing
        */
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        var errorResponseInputSource = new InputSource(new StringReader(errorResponse));
        DocumentBuilder builder;
        Document errorResponseDocument;

        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }

        try {
            errorResponseDocument = builder.parse(errorResponseInputSource);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
            return null;
        }

        var nList = errorResponseDocument.getElementsByTagName(attribute);
        return nList.item(0).getTextContent();
    }

    /**
     * Creates and returns an error response.
     * @param errorMessage
     * @param error
     * @return TransformedObject error response.
     */
    public static TransformedObject getErrorResponse(String errorMessage, Error error) {
        var responseObject = new TransformedObject();

        responseObject.setHasError(true);
        responseObject.setErrorMessage(errorMessage);
        responseObject.setErrorCode(new ResponseErrorCode().getErrorCode().get(error));
        responseObject.setStatusCode(new ResponseErrorCode().getStatusCode().get(error));

        return responseObject;
    }
}
