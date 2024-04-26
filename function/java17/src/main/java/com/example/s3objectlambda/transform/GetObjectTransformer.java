package com.example.s3objectlambda.transform;


import com.example.s3objectlambda.exception.InvalidPartNumberException;
import com.example.s3objectlambda.exception.InvalidRangeException;
import com.example.s3objectlambda.exception.TransformationException;
import com.example.s3objectlambda.request.GetObjectRequestWrapper;

import java.net.URISyntaxException;
import java.util.Optional;

/**
 * This is the transformer class for getObject requests.
 */

public class GetObjectTransformer implements Transformer {
    private GetObjectRequestWrapper userRequest;
    public GetObjectTransformer(GetObjectRequestWrapper userRequest) {
        this.userRequest = userRequest;
    }

    /**
     * TODO: Implement your transform object logic here.
     *
     * @param responseObjectByteArray object response as byte array to be transformed.
     * @return Transformed object as byte array.
     */
    public byte[] transformObjectResponse(byte[] responseObjectByteArray) throws TransformationException {

        /**
         * Add your code to transform the responseObjectByteArray.
         * After transforming the responseObjectByteArray just return the transformed byte array.
         */

        return responseObjectByteArray;
    }

    /**
     *
     * @param responseObjectByteArray Response object as byte array on which range/part number to be applied.
     * @param userRequest GetObjectRequest object
     * @return Returns responseObjectByteArray
     * @throws URISyntaxException
     * @throws InvalidRangeException
     * @throws InvalidPartNumberException
     */

    @Override
    public byte[] applyRangeOrPartNumber(byte[] responseObjectByteArray)
            throws URISyntaxException, InvalidRangeException, InvalidPartNumberException {
        Optional<String> range = this.userRequest.getRange();
        Optional<String> partNumber = this.userRequest.getPartNumber();

        if (range.isPresent()) {
            return new RangeMapper(range.get()).mapRange(responseObjectByteArray);
        } else if (partNumber.isPresent()) {
            return new PartNumberMapper().mapPartNumber(partNumber.get(), responseObjectByteArray);
        } else {
            return responseObjectByteArray;
        }
    }
}
