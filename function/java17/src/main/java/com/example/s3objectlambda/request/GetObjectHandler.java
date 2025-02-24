package com.example.s3objectlambda.request;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.example.s3objectlambda.exception.InvalidUrlException;
import com.example.s3objectlambda.exception.TransformationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.s3objectlambda.error.Error;
import com.example.s3objectlambda.exception.InvalidPartNumberException;
import com.example.s3objectlambda.exception.InvalidRangeException;
import com.example.s3objectlambda.response.ResponseHandler;
import com.example.s3objectlambda.transform.Transformer;
import com.example.s3objectlambda.validator.RequestValidator;

/**
 * Handles a GetObject request, by performing the following steps:
 * 1. Validates the incoming user request.
 * 2. Retrieves the original object from Amazon S3.
 * 3. Applies a transformation. You can apply your custom transformation logic here.
 * (GetObjectTransformer::transformObjectResponse()).
 * 4. Handles post-processing of the transformation, such as applying range or part numbers.
 * 5. Sends the final transformed object back to Amazon S3 Object Lambda.
 */

public class GetObjectHandler implements RequestHandler {

    private final Logger logger;
    private final AmazonS3 s3Client;
    private final Transformer transformer;
    private final RequestValidator requestValidator;
    private final ResponseHandler responseHandler;
    private final S3ObjectLambdaEvent s3ObjectLambdaEvent;
    private final HttpClient httpClient;



    public GetObjectHandler(AmazonS3 s3Client, Transformer transformer,
                            RequestValidator requestValidator,
                            S3ObjectLambdaEvent event, ResponseHandler responseHandler, HttpClient httpClient) {

        this.s3Client = s3Client;
        this.transformer = transformer;
        this.requestValidator = requestValidator;
        this.responseHandler = responseHandler;
        this.s3ObjectLambdaEvent = event;
        this.httpClient = httpClient;
        this.logger = LoggerFactory.getLogger(GetObjectHandler.class);
    }

    @Override
    public void handleRequest() {

        // Validate user request and return error if invalid
        var validationError = this.requestValidator.validateUserRequest();
        if (validationError.isPresent()) {
            this.responseHandler.writeErrorResponse(validationError.get(), Error.INVALID_REQUEST);
            return;
        }


        // Get the original object from Amazon S3
        HttpResponse<InputStream> presignedResponse;
        try {
            presignedResponse = this.getS3ObjectResponse(this.s3ObjectLambdaEvent.inputS3Url());
        } catch (URISyntaxException | IOException | InterruptedException | InvalidUrlException e) {
            this.logger.error("Error while getting the s3 object: " + e);
            this.responseHandler.writeErrorResponse("Error occurred while getting the object.",
                    Error.SERVER_ERROR);
            return;
        }

        // Ideally, Errors in the Amazon S3 response should be forwarded to the caller without invoking transformObject.
        if (presignedResponse.statusCode() >= 400) {
            this.responseHandler.writeS3GetObjectErrorResponse(presignedResponse);
            return;
        }

        byte[] objectResponseByteArray;
        try {
            objectResponseByteArray = presignedResponse.body().readAllBytes();
        } catch (IOException e) {
            logger.error("Error while reading the presigned response body." + e);
            this.responseHandler.writeErrorResponse("Error occurred while getting the data.",
                    Error.SERVER_ERROR);
            return;
        }

        //Transform the object response.
        byte[] transformedObject;
        try {
            transformedObject = this.transformer.transformObjectResponse(objectResponseByteArray);
        } catch (TransformationException e) {
            logger.error("Error while transforming the object." + e);
            this.responseHandler.writeErrorResponse("Error transforming the object.", e.getError());
            return;
        }



        /*
         The most reliable way to handle Range or partNumber requests is to retrieve the full object from S3,
         transform the object, and then apply the requested Range or partNumber parameters to the transformed object.
         https://docs.aws.amazon.com/AmazonS3/latest/userguide/olap-writing-lambda.html#range-get-olap
         Handle range or partNumber if present in the request.
         */
        byte[] transformedObjectWithRange;
        try {
            transformedObjectWithRange = this.transformer.applyRangeOrPartNumber(transformedObject);
        } catch (URISyntaxException e) {
            this.logger.error("Exception while in applyRangeOrPartNumber: " + e);
            this.responseHandler.writeErrorResponse("Unexpected error while transforming the object:",
                    Error.SERVER_ERROR);
            return;
        } catch (InvalidRangeException e) {
            this.logger.error("Invalid Range Exception: " + e);
            this.responseHandler.writeErrorResponse(e.getMessage(), e.getError());
            return;
        } catch (InvalidPartNumberException e) {
            this.logger.error("Invalid partNumber: " + e);
            this.responseHandler.writeErrorResponse(e.getMessage(), e.getError());
            return;
        }

        this.responseHandler.writeObjectResponse(presignedResponse, transformedObjectWithRange);
    }

    private HttpRequest prepareHttpRequest(final String s3PresignedUrl)
        throws URISyntaxException, InvalidUrlException {

        var httpRequestBuilder = HttpRequest.newBuilder(new URI(s3PresignedUrl));
        var userRequestHeaders = this.s3ObjectLambdaEvent.getUserRequest().getHeaders();

        // If a header is signed, then it must be included in the actual http call.
        // Otherwise, the lambda will get a signature error response.
        includeSignedHeadersToHttpRequest(s3PresignedUrl, userRequestHeaders, httpRequestBuilder);

        // Some headers are not signed, but should be passed via a presigned url call to ensure desired behaviour.
        includeUserHeadersToHttpRequest(userRequestHeaders, httpRequestBuilder);

        return httpRequestBuilder.GET().build();
    }

    private static void includeUserHeadersToHttpRequest(
        final Map<String, String> userRequestHeaders, HttpRequest.Builder httpRequestBuilder) {

        var optionalHeaders = Arrays.asList(
            "If-Match",
            "If-Modified-Since",
            "If-None-Match",
            "If-Unmodified-Since");

        for (var headerKey : optionalHeaders) {
            if (userRequestHeaders.containsKey(headerKey)) {
                httpRequestBuilder.header(headerKey, userRequestHeaders.get(headerKey));
            }
        }
    }

    private static void includeSignedHeadersToHttpRequest(
        final String s3PresignedUrl, final Map<String, String> userRequestHeaders,
        HttpRequest.Builder httpRequestBuilder) throws InvalidUrlException {

        List<String> signedHeaders =
            S3PresignedUrlParserHelper.retrieveSignedHeadersFromPresignedUrl(s3PresignedUrl);

        for (var userRequestHeader : userRequestHeaders.entrySet()) {
            if (signedHeaders.contains(userRequestHeader.getKey())) {
                httpRequestBuilder.header(userRequestHeader.getKey(), userRequestHeader.getValue());
            }
        }
    }

    private HttpResponse<InputStream> getS3ObjectResponse(String s3PresignedUrl)
        throws URISyntaxException, IOException, InterruptedException, InvalidUrlException {

        HttpRequest request = prepareHttpRequest(s3PresignedUrl);

        return this.httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofInputStream());
    }
}
