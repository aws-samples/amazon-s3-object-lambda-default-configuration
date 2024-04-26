package com.example.s3objectlambda;

import com.example.s3objectlambda.checksum.Md5Checksum;
import com.example.s3objectlambda.request.GetObjectHandler;
import com.example.s3objectlambda.request.GetObjectRequestWrapper;
import com.example.s3objectlambda.response.GetObjectResponseHandler;
import com.example.s3objectlambda.transform.GetObjectTransformer;
import com.example.s3objectlambda.validator.GetObjectRequestValidator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.net.http.HttpClient;

/**
 * This is the main handler for your lambda function.
 **/

public class Handler {

    /**
     * <p>The event object contains all information required to handle a request from Amazon S3 Object Lambda.</p>
     *
     * <p>The event object contains information about the GetObject request
     * which resulted in this Lambda function being invoked.</p>
     *
     * <p>The userRequest (event.getUserRequest()) object contains information
     * related to the entity (user or application)
     * that invoked Amazon S3 Object Lambda. This information can be used in multiple ways, for example,
     * to allow or deny the request based on the entity.
     * See the <i>Respond with a 403 Forbidden</i> example in
     * <a href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/olap-writing-lambda.html"> Writing Lambda functions</a>
     * for sample code.</p>
     */


    private AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();

    public void handleRequest(S3ObjectLambdaEvent event, Context context) throws Exception {


        /*
        You can call handler from here depending on what the handler does and what the request is for.
        In this case, if the event has GetObjectContext we call the GetObjectHandler implementation.
        */

        if (event.getGetObjectContext() != null) {

            var responseHandler = new GetObjectResponseHandler(this.s3Client, event, new Md5Checksum());
            var userRequest = new GetObjectRequestWrapper(event.getUserRequest());
            var requestValidator = new GetObjectRequestValidator(userRequest);
            var transformer = new GetObjectTransformer(userRequest);
            var httpClient = HttpClient.newBuilder().build();

            new GetObjectHandler(this.s3Client,
                    transformer,
                    requestValidator,
                    event,
                    responseHandler,
                    httpClient).handleRequest();
        }
    }
}
