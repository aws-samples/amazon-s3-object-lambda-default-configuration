package com.amazon.s3objectlambda.request;

import com.amazon.s3objectlambda.error.Error;
import com.amazon.s3objectlambda.response.ObjectResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

import static com.amazon.s3objectlambda.request.RequestValidator.validateUserRequest;
import static com.amazon.s3objectlambda.response.ResponseUtil.getErrorResponse;
import static com.amazon.s3objectlambda.transform.ResponseTransformer.applyRangeOrPartNumber;
import static com.amazon.s3objectlambda.transform.ResponseTransformer.transformObjectResponse;

/**
 * Handles a GetObject request, by performing the following steps:
 * 1. Validates the incoming user request.
 * 2. Retrieves the original object from Amazon S3.
 * 3. Applies a transformation. You can apply your custom transformation logic here.
 *    (ResponseTransformation::transformObjectResponse()).
 * 4. Handles post-processing of the transformation, such as applying range or part numbers.
 * 5. Sends the final transformed object back to Amazon S3 Object Lambda.
 */

public class GetObjectHandler implements RequestHandler {

    @Override
    public void handleRequest(S3ObjectLambdaEvent event, Context context) throws Exception {
        var userRequest = event.getUserRequest();
        var objectResponse = new ObjectResponse(S3_CLIENT, event);

        // Validate user request and return error if invalid
        var validationOutput = validateUserRequest(userRequest);
        if (validationOutput.isPresent()) {
            writeValidationFailedErrorResponse(validationOutput.get(), objectResponse);
            return;
        }

        // Read the original object from Amazon S3
        var presignedResponse = getS3ObjectResponse(event, userRequest);

        // Errors in the Amazon S3 response should be forwarded to the caller without invoking transformObject.
        if (presignedResponse.statusCode() >= 400) {
            objectResponse.writeS3GetObjectErrorResponse(presignedResponse);
            return;
        }

        var transformedObject = transformObjectResponse(presignedResponse.body().readAllBytes());
        if (transformedObject.getHasError()) {
            objectResponse.writeErrorResponse(transformedObject);
            return;
        }
        /*
        The most reliable way to handle Range or partNumber requests is to retrieve the full object from S3,
        transform the object, and then apply the requested Range or partNumber parameters to the transformed object.
        https://docs.aws.amazon.com/AmazonS3/latest/userguide/olap-writing-lambda.html#range-get-olap
        Handle range or partNumber if present in the request
        */
        var transformedObjectWithRange = applyRangeOrPartNumber(
                transformedObject.getObjectResponse(),
                userRequest);
        if (transformedObjectWithRange.getHasError()) {
            objectResponse.writeErrorResponse(transformedObjectWithRange);
            return;
        }

        objectResponse.writeObjectResponse(presignedResponse, transformedObjectWithRange.getObjectResponse());
    }

    /**
     * Get the original s3 object using the presigned URL in the user request.
     */
    private HttpResponse<InputStream> getS3ObjectResponse(S3ObjectLambdaEvent event,
                                                          S3ObjectLambdaEvent.UserRequest userRequest)
            throws URISyntaxException, IOException, InterruptedException {
        var httpClient = HttpClient.newBuilder().build();
        var httpRequestBuilder = HttpRequest.newBuilder(new URI(event.inputS3Url()));
        var userRequestHeaders = userRequest.getHeaders();
        var headersToBePresigned = Arrays.asList(
                "x-amz-expected-bucket-owner",
                "If-Match",
                "If-Modified-Since",
                "If-None-Match",
                "If-Unmodified-Since");

        for (var userRequestHeader : userRequestHeaders.entrySet()) {
            if (headersToBePresigned.contains(userRequestHeader.getKey())) {
                httpRequestBuilder.header(userRequestHeader.getKey(), userRequestHeader.getValue());
            }
        }

        var presignedResponse = httpClient.send(
                httpRequestBuilder.GET().build(),
                HttpResponse.BodyHandlers.ofInputStream());
        return presignedResponse;
    }

    /**
     * Get the error response object for validation error and write the error response.
     */
    private void writeValidationFailedErrorResponse(String validationOutput, ObjectResponse objectResponse) {
        var responseObject = getErrorResponse(validationOutput, Error.INVALID_REQUEST);
        objectResponse.writeErrorResponse(responseObject);
        return;
    }
}
