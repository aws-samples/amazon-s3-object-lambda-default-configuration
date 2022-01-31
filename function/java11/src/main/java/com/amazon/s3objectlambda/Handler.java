package com.amazon.s3objectlambda;

import com.amazon.s3objectlambda.request.GetObjectHandler;
import com.amazon.s3objectlambda.request.RequestHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;

public class Handler implements RequestHandler {
    @Override
    public void handleRequest(S3ObjectLambdaEvent event, Context context) {

        try {
            new GetObjectHandler().handleRequest(event, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
