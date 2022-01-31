package com.amazon.s3objectlambda.transform;

import com.amazon.s3objectlambda.error.Error;
import com.amazon.s3objectlambda.response.ResponseUtil;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;


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

    private static String supportedUnit = "bytes";

    public TransformedObject mapRange(String rangeHeaderValue, byte[] responseObjectByteArray) {

        var rangePattern = "([a-z]+)=(\\d+)?-(\\d+)?";
        var rangeRegexPattern = Pattern.compile(rangePattern);
        var rangeRegexMatcher = rangeRegexPattern.matcher(rangeHeaderValue);

        if (!rangeRegexMatcher.find()) {
            return ResponseUtil.getErrorResponse("Invalid Range", Error.INVALID_RANGE);
        }

/*
        First part of the range from regex match.
        This contains the unit in which range is requested.
*/
        var unit = rangeRegexMatcher.group(1);
        if (!Objects.equals(unit, supportedUnit)) {
            return ResponseUtil.getErrorResponse(String.format("Only %s as unit supported but %s was provided ",
                    supportedUnit, unit), Error.INVALID_RANGE);
        }

/*
        Second and third part from the range value are <range-start> and <range-end>
        Read this from the regex matcher.
*/
        var rangeFirstPart = rangeRegexMatcher.group(2);
        var rangeLastPart = rangeRegexMatcher.group(3);

        int rangeStart;
        int rangeEnd;

        if (rangeFirstPart == null && rangeLastPart == null) {
            return ResponseUtil.getErrorResponse("Invalid Range", Error.INVALID_RANGE);
        }

        int objectLength = responseObjectByteArray.length;

        if (rangeFirstPart == null) {
            /* Range request was of the form <unit>=-<suffix-length> so we return the last `suffix-length` bytes. */
            int suffixLength = Integer.parseInt(rangeLastPart);

            /*
             If the byte array length is 26,
             the last byte is at 25th position in the array.
            */
            rangeEnd = objectLength - 1;
            rangeStart = objectLength - suffixLength;
        } else if (rangeLastPart == null) {
            /* Range request was of the form <unit>=<range-start>- so we return from range-start to the end
        of the object. */
            rangeStart = Integer.parseInt(rangeFirstPart);
            rangeEnd = objectLength - 1;
        } else {
            rangeStart = Integer.parseInt(rangeFirstPart);
            rangeEnd = Integer.parseInt(rangeLastPart);
            rangeEnd = Math.min(objectLength - 1, rangeEnd); // Should not exceed object length
        }

        if (rangeEnd < rangeStart || rangeStart < 0) {
            return ResponseUtil.getErrorResponse("Invalid Range", Error.INVALID_RANGE);
        }


//        Add 1 at the range end because Arrays.copyOfRange's is exclusive.
        byte[] objectPart = Arrays.copyOfRange(responseObjectByteArray, rangeStart, rangeEnd + 1);
        var responseObject = new TransformedObject();
        responseObject.setHasError(false);
        responseObject.setObjectResponse(objectPart);

        return responseObject;
    }
}
