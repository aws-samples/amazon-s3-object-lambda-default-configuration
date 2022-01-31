package com.example.s3objectlambda.transform;

import com.example.s3objectlambda.exception.InvalidRangeException;

import java.util.Arrays;
import java.util.Objects;


/**
 * Handles range requests by applying the range to the transformed object. Supported range headers are:
 * <p>
 * Range: <unit>=<range-start>-
 * Range: <unit>=<range-start>-<range-end>
 * Range: <unit>=-<suffix-length>
 * <p>
 * Amazon S3 does not support retrieving multiple ranges of data per GetObject request. Please see
 * <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObject.html#API_GetObject_RequestSyntax|GetObject">Request Syntax</a>}.
 * for more information.
 * <p>
 * The only supported unit in this implementation is `bytes`. If other units are requested, we treat this as
 * an invalid request.
 */

public class RangeMapper {

    private static final String BYTES_UNIT = "bytes";
    private final String[] supportedUnits = {BYTES_UNIT};
    private Range range;

    /**
     * This constructor accepts range as string and instantiate the object after instantiating Range.
     * @param range range as string in one of the format supported.
     * @throws InvalidRangeException
     */
    public RangeMapper(String range) throws InvalidRangeException {
        this.range = new Range(range);
    }

    /**
     * This constructor accepts range object.
     * @param range Range object.
     */
    public RangeMapper(Range range) {
        this.range = range;
    }

    /**
     * This function apply range on the response object byte array.
     * @param responseObjectByteArray Response object on which range to be applied.
     * @return Returns result byte array after range is applied.
     * @throws InvalidRangeException
     */
    public byte[] mapRange(byte[] responseObjectByteArray) throws InvalidRangeException {

        if (!Arrays.asList(supportedUnits).contains(this.range.getUnit())) {
            throw new InvalidRangeException(String.format("Only %s as unit supported but %s was provided.",
                    supportedUnits, this.range.getUnit()));
        }

        if (Objects.equals(this.range.getUnit(), BYTES_UNIT)) {
            return applyRangeOnBytes(responseObjectByteArray);
        }

        throw new RuntimeException("Not implemented range unit support:" + this.range.getUnit());
    }

    private byte[] applyRangeOnBytes(byte[] responseObjectByteArray) throws InvalidRangeException {
        int rangeStart;
        int rangeEnd;
        int objectLength = responseObjectByteArray.length;

        if (this.range.getFirstPart() == null) {
//          Range request was of the form <unit>=-<suffix-length> so we return the last `suffix-length` bytes.
            int suffixLength = Integer.parseInt(this.range.getLastPart());

//          If the byte array length is 26, the last byte is at 25th position in the array.
            rangeEnd = objectLength - 1;
            rangeStart = objectLength - suffixLength;

        } else if (this.range.getLastPart() == null) {
//          Range request was of the form <unit>=<range-start>- so we return from range-start to the end of the object.
            rangeStart = Integer.parseInt(this.range.getFirstPart());
            rangeEnd = objectLength - 1;
        } else {
            rangeStart = Integer.parseInt(this.range.getFirstPart());
            rangeEnd = Integer.parseInt(this.range.getLastPart());
            rangeEnd = Math.min(objectLength - 1, rangeEnd); // Should not exceed object length
        }

        if (rangeEnd < rangeStart || rangeStart < 0) {
            throw new InvalidRangeException("Invalid Range");
        }

        //Add 1 at the range end because Arrays.copyOfRange's is exclusive.
        var objectPart = Arrays.copyOfRange(responseObjectByteArray, rangeStart, rangeEnd + 1);
        return objectPart;
    }
}
