package com.amazon.s3objectlambda.transform;

import com.amazon.s3objectlambda.response.ResponseUtil;
import com.amazon.s3objectlambda.error.Error;

import java.util.Arrays;

/**
 * Handles partNumber requests by applying the partNumber to the transformed object.
 */
public class PartNumberMapper {

    private final Integer partSize = 5242880;

    public TransformedObject mapPartNumber(String partNumber, byte[] responseObjectByteArray) {

        double objectLength;
        TransformedObject responseObject = new TransformedObject();
        objectLength = responseObjectByteArray.length;
        double totalParts = Math.ceil(objectLength / this.partSize);
        int requestedPart;

        try {
            requestedPart = Integer.parseInt(partNumber);
        } catch (NumberFormatException nfe) {
            return ResponseUtil.getErrorResponse("Invalid part number.", Error.INVALID_PART);
        }

        if (requestedPart > totalParts || requestedPart <= 0) {
            return ResponseUtil.getErrorResponse(String.format("Cannot specify part number: %s. " +
                    "Use part number from 1 to %s.", requestedPart, totalParts), Error.INVALID_PART);
        }

        int partStart = (requestedPart - 1) * this.partSize;
        int partEnd = (int) Math.min(partStart + this.partSize, objectLength);
        byte[] objectPart = Arrays.copyOfRange(responseObjectByteArray, partStart, partEnd);

        responseObject.setHasError(false);
        responseObject.setObjectResponse(objectPart);

        return responseObject;
    }
}
