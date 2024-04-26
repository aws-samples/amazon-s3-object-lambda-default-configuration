package com.example.s3objectlambda.response;

import com.example.s3objectlambda.checksum.Checksum;
import com.example.s3objectlambda.checksum.ChecksumGenerator;
import com.example.s3objectlambda.error.Error;
import com.example.s3objectlambda.error.S3RequestError;
import com.example.s3objectlambda.error.XMLErrorParser;
import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.WriteGetObjectResponseRequest;
import com.amazonaws.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.HashMap;

/**
 * This handles writing of the object response by calling writeGetObjectResponse
 * which Passes transformed objects to a GetObject operation.
 */

public class GetObjectResponseHandler implements ResponseHandler {

    private Logger logger;
    private final AmazonS3 s3Client;
    private final S3ObjectLambdaEvent event;
    private final ChecksumGenerator checksumGenerator;

    public GetObjectResponseHandler(AmazonS3 s3Client, S3ObjectLambdaEvent event, ChecksumGenerator checksumGenerator) {
        this.s3Client = s3Client;
        this.event = event;
        this.checksumGenerator = checksumGenerator;
        this.logger = LoggerFactory.getLogger(GetObjectResponseHandler.class);
    }

    public void writeS3GetObjectErrorResponse(HttpResponse<InputStream> presignedResponse) {

        S3RequestError s3errorResponse;

        try {
            var xmlResponse = IOUtils.toString(presignedResponse.body());
            s3errorResponse = new XMLErrorParser().parse(xmlResponse);
        } catch (IOException | ParserConfigurationException | SAXException | NullPointerException e) {
            this.logger.error("Error while reading the S3 error response body: " + e);
            writeErrorResponse("Unexpected error while reading the S3 error response", Error.SERVER_ERROR);
            return;
        }


        this.s3Client.writeGetObjectResponse(new WriteGetObjectResponseRequest()
                .withRequestRoute(this.event.outputRoute())
                .withRequestToken(this.event.outputToken())
                .withErrorCode(s3errorResponse.getCode())
                .withContentLength(0L).withInputStream(new ByteArrayInputStream(new byte[0]))
                .withErrorMessage(s3errorResponse.getMessage())
                .withStatusCode(presignedResponse.statusCode()));
    }


    public void writeErrorResponse(String errorMessage, Error error) {

        this.s3Client.writeGetObjectResponse(new WriteGetObjectResponseRequest()
                .withRequestRoute(event.outputRoute())
                .withRequestToken(event.outputToken())
                .withErrorCode(error.getErrorCode())
                .withContentLength(0L).withInputStream(new ByteArrayInputStream(new byte[0]))
                .withErrorMessage(errorMessage)
                .withStatusCode(error.getStatusCode()));
    }

    public void writeObjectResponse(HttpResponse<InputStream> presignedResponse, byte[] responseObjectByteArray) {

        Checksum checksum;
        try {
            checksum = this.checksumGenerator.getChecksum(responseObjectByteArray);
        } catch (Exception e) {
            this.logger.error("Error while writing object response" + e);
            writeErrorResponse("Error while writing object response.", Error.SERVER_ERROR);
            return;
        }

        var checksumMap = new HashMap<String, String>();
        checksumMap.put("algorithm", checksum.getAlgorithm());
        checksumMap.put("digest", checksum.getChecksum());

        var checksumObjectMetaData = new ObjectMetadata();
        checksumObjectMetaData.setUserMetadata(checksumMap);

        this.s3Client.writeGetObjectResponse(new WriteGetObjectResponseRequest()
                .withRequestRoute(event.outputRoute())
                .withRequestToken(event.outputToken())
                .withInputStream(new ByteArrayInputStream(responseObjectByteArray))
                .withMetadata(checksumObjectMetaData)
                .withStatusCode(presignedResponse.statusCode()));
    }

}
