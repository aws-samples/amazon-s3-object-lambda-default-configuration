package com.example.s3objectlambda.transform;

import com.example.s3objectlambda.exception.InvalidRangeException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the Range object.
 * Range can be represented in one of the following formats.
 *  * <unit>=<range-start>-
 *  * <unit>=<range-start>-<range-end>
 *  * <unit>=-<suffix-length>
 *  The object has lastPart, firstPart and unit.
 *  firstPart or lastPart can be empty depending on the type of range request ir represents.
 */
public class Range {

    private String lastPart;
    private String firstPart;
    private String unit;

    /**
     * This constructor accepts range as string in one of the valid formats.
     * @param range range as string.
     * @throws InvalidRangeException
     */
    public Range(String range) throws InvalidRangeException {

        var rangeRegexMatcher = getRangeRegexMatcher(range);
        //First part of the range from regex match. This contains the unit in which range is requested.
        this.unit = rangeRegexMatcher.group(1);

        //Second and third part from the range value are <range-start> and <range-end>
        //Read this from the regex matcher.
        this.firstPart = rangeRegexMatcher.group(2);
        this.lastPart = rangeRegexMatcher.group(3);

        if (this.firstPart == null && this.lastPart == null) {
            throw new InvalidRangeException("No values found for start and end.");
        }
    }

    private Matcher getRangeRegexMatcher(String range) throws InvalidRangeException {
        var rangePattern = "([a-z]+)=(\\d+)?-(\\d+)?";
        var rangeRegexPattern = Pattern.compile(rangePattern);
        var rangeRegexMatcher = rangeRegexPattern.matcher(range);

        if (!rangeRegexMatcher.find()) {
            throw new InvalidRangeException("Invalid Range: " + range);
        }
        return rangeRegexMatcher;
    }

    public String getLastPart() {
        return lastPart;
    }

    public String getFirstPart() {
        return firstPart;
    }

    public String getUnit() {
        return unit;
    }

}
