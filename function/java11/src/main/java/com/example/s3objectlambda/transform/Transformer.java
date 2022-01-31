package com.example.s3objectlambda.transform;


import com.example.s3objectlambda.exception.InvalidPartNumberException;
import com.example.s3objectlambda.exception.InvalidRangeException;
import com.example.s3objectlambda.exception.TransformationException;

import java.net.URISyntaxException;

/**
 * This interface should be implemented by the class that transforms the response.
 */
public interface Transformer {
    byte[] transformObjectResponse(byte[] responseObjectByteArray) throws TransformationException;

    byte[] applyRangeOrPartNumber(byte[] responseObjectByteArray)
            throws URISyntaxException, InvalidRangeException, InvalidPartNumberException;
}
