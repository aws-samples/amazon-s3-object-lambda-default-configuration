package com.amazon.s3objectlambda.response;

import com.amazon.s3objectlambda.checksum.Checksum;
import com.amazon.s3objectlambda.transform.TransformedObject;
import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.WriteGetObjectResponseRequest;
import com.amazonaws.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;

import static com.amazon.s3objectlambda.response.ResponseUtil.getAttributeFromS3ErrorResponse;

/**
 *  This handles writing of the object response by calling writeGetObjectResponse
 *  which Passes transformed objects to a GetObject operation.
 *
 */
public class ObjectResponse {

    private final AmazonS3 s3Client;
    private final S3ObjectLambdaEvent event;

    public ObjectResponse(AmazonS3 s3Client, S3ObjectLambdaEvent event) {
        this.s3Client = s3Client;
        this.event = event;
    }

    public void writeS3GetObjectErrorResponse(HttpResponse<InputStream> presignedResponse) {

        String xmlResponse;

        try {
            xmlResponse = IOUtils.toString(presignedResponse.body());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        var errorCode = getAttributeFromS3ErrorResponse(xmlResponse, "Code");
        var errorMessage = getAttributeFromS3ErrorResponse(xmlResponse, "Message");

        this.s3Client.writeGetObjectResponse(new WriteGetObjectResponseRequest()
                .withRequestRoute(this.event.outputRoute())
                .withRequestToken(this.event.outputToken())
                .withErrorCode(errorCode)
                .withContentLength(0L).withInputStream(new ByteArrayInputStream(new byte[0]))
                .withErrorMessage(errorMessage)
                .withStatusCode(presignedResponse.statusCode()));
    }

    public void writeErrorResponse(TransformedObject transformedObject) {
        s3Client.writeGetObjectResponse(new WriteGetObjectResponseRequest()
                .withRequestRoute(event.outputRoute())
                .withRequestToken(event.outputToken())
                .withErrorCode(transformedObject.getErrorCode())
                .withContentLength(0L).withInputStream(new ByteArrayInputStream(new byte[0]))
                .withErrorMessage(transformedObject.getErrorMessage())
                .withStatusCode(transformedObject.getStatusCode()));
    }

    public void writeObjectResponse(HttpResponse<InputStream> presignedResponse, byte[] responseObjectByteArray) {

        var objectChecksum = Checksum.getChecksum(responseObjectByteArray);
        var checksumObjectMetaData = new ObjectMetadata();
        checksumObjectMetaData.setUserMetadata(objectChecksum);

        s3Client.writeGetObjectResponse(new WriteGetObjectResponseRequest()
                .withRequestRoute(event.outputRoute())
                .withRequestToken(event.outputToken())
                .withInputStream(new ByteArrayInputStream(responseObjectByteArray))
                .withMetadata(checksumObjectMetaData)
                .withStatusCode(presignedResponse.statusCode()));
    }
}
