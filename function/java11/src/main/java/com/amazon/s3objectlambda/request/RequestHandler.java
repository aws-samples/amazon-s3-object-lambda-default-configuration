package com.amazon.s3objectlambda.request;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

public interface RequestHandler {
    AmazonS3 S3_CLIENT = AmazonS3Client.builder().build();
    void handleRequest(S3ObjectLambdaEvent event, Context context) throws Exception;
}
