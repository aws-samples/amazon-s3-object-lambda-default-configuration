package com.example.s3objectlambda.response;

import com.example.s3objectlambda.error.Error;

import java.io.InputStream;
import java.net.http.HttpResponse;

/**
 * This interface represents the response handler.
 * The implementing class will be handling updating of the get object response with the transformed object
 * and writing error response.
 */
public interface ResponseHandler {
    void writeS3GetObjectErrorResponse(HttpResponse<InputStream> presignedResponse);
    void writeErrorResponse(String errorMessage, Error error);
    void writeObjectResponse(HttpResponse<InputStream> presignedResponse, byte[] responseObjectByteArray);
}
