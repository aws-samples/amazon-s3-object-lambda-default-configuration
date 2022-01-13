package com.amazon.s3objectlambda.transform;


import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent;

import static com.amazon.s3objectlambda.request.RequestUtil.getPartNumber;
import static com.amazon.s3objectlambda.request.RequestUtil.getRange;

public final class ResponseTransformer {

    private ResponseTransformer() {

    }

    /**
     * TODO: Implement your transform object logic here.
     * This accepts responseObjectByteArray and returns TransformedObject instance.
     * @param responseObjectByteArray
     * @return instance of TransformedObject.
     */
    public static TransformedObject transformObjectResponse(byte[] responseObjectByteArray) {
        TransformedObject response = new TransformedObject();
        response.setHasError(false);

        /**
         * Add your code to transform the responseObjectByteArray.
         * After transforming the responseObjectByteArray, set it to TransformedObject response by
         * response.setObjectResponse()
         *
         * If any error was encountered, and you want to return error response,
         * you can get error response by calling ResponseUtil.getErrorResponse(),
         * And return that response.
         *
         */
        response.setObjectResponse(responseObjectByteArray);
        return response;
    }

    /**
     * Apply range or partNumber on the responseByteArray based on usersRequest.
     * @param responseObjectByteArray
     * @param userRequest
     * @return instance of TransformedObject.
     */
    public static TransformedObject applyRangeOrPartNumber(
            byte[] responseObjectByteArray, S3ObjectLambdaEvent.UserRequest userRequest) {
        String range = getRange(userRequest);
        String partNumber = getPartNumber(userRequest);

        if (range != null) {
            return new RangeMapper().mapRange(range, responseObjectByteArray);
        } else if (partNumber != null) {
            return new PartNumberMapper().mapPartNumber(partNumber, responseObjectByteArray);
        } else {
            TransformedObject responseObject = new TransformedObject();
            responseObject.setHasError(false);
            responseObject.setObjectResponse(responseObjectByteArray);
            return responseObject;
        }
    }
}
