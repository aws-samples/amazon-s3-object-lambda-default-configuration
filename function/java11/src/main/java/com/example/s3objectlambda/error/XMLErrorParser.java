package com.example.s3objectlambda.error;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * This class is the parser for the xml error response from S3 getObject request.
 */
public class XMLErrorParser implements ErrorParser {

    @Override
    public S3RequestError parse(String errorResponse) throws
            ParserConfigurationException, SAXException, IOException {
        var errorResponseDocument = getErrorResponseDocument(errorResponse);
        var s3RequestError = new S3RequestError();
        s3RequestError.setCode(getErrorAttributeValue(errorResponseDocument, "Code"));
        s3RequestError.setMessage(getErrorAttributeValue(errorResponseDocument, "Message"));
        s3RequestError.setRequestId(getErrorAttributeValue(errorResponseDocument, "RequestId"));

        return s3RequestError;
    }

    private String getErrorAttributeValue(Document errorResponseDocument, String attribute) {
        var nList = errorResponseDocument.getElementsByTagName(attribute);
        var attributeValue = nList.item(0).getTextContent();
        return attributeValue;
    }

    private Document getErrorResponseDocument(String errorResponse) throws
            ParserConfigurationException, SAXException, IOException {
        var factory = DocumentBuilderFactory.newInstance();
        /*
        Prevent XML External Entity (XXE) Processing
        https://owasp.org/www-community/vulnerabilities/XML_External_Entity_(XXE)_Processing
        */

        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);

        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        var errorResponseInputSource = new InputSource(new StringReader(errorResponse));
        DocumentBuilder builder;
        Document errorResponseDocument;

        builder = factory.newDocumentBuilder();
        errorResponseDocument = builder.parse(errorResponseInputSource);

        return errorResponseDocument;
    }
}
