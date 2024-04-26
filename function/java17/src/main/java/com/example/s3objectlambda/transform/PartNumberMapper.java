package com.example.s3objectlambda.transform;

import com.example.s3objectlambda.exception.InvalidPartNumberException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Handles partNumber requests by applying the partNumber to the transformed object.
 */
public class PartNumberMapper {

    /*
    Minimum part size in a multipart upload is 5 MB.
    There is no size limit on the last part of the multipart upload.
     */
    private final Integer partSize = 5242880;
    private Logger logger;

    public PartNumberMapper() {
        this.logger = LoggerFactory.getLogger(PartNumberMapper.class);
    }

    /**
     * This method returns the requested part from the response object.
     * @param partNumber Part number , this should be >0 and <= Total number of parts in the response object.
     * @param responseObjectByteArray Response object as byte array, from which a particular part is requested.
     * @return Returns the byte array object of the requested part.
     * @throws InvalidPartNumberException
     */
    public byte[] mapPartNumber(String partNumber, byte[] responseObjectByteArray)
            throws InvalidPartNumberException {

        double objectLength;
        objectLength = responseObjectByteArray.length;
        double totalParts = Math.ceil(objectLength / this.partSize);
        int requestedPart;

        try {
            requestedPart = Integer.parseInt(partNumber);
        } catch (NumberFormatException nfe) {
            this.logger.error("Invalid partNumber" + nfe);
            throw new InvalidPartNumberException("Invalid partNumber: " + partNumber);
        }

        if (requestedPart > totalParts || requestedPart <= 0) {
            throw new InvalidPartNumberException(String.format("Cannot specify part number: %s. " +
                    "Use part number from 1 to %s.", requestedPart, totalParts));
        }

        int partStart = (requestedPart - 1) * this.partSize;
        int partEnd = (int) Math.min(partStart + this.partSize, objectLength);
        var objectPart = Arrays.copyOfRange(responseObjectByteArray, partStart, partEnd);
        return objectPart;

    }
}
