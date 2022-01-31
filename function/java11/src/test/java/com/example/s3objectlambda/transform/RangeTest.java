package com.example.s3objectlambda.transform;

import com.example.s3objectlambda.exception.InvalidRangeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;


public class RangeTest {
    @Test
    void testGetFirstPart() throws InvalidRangeException {
        var range = new Range("byte=12-25");
        assertEquals("12", range.getFirstPart());
    }

    @Test
    @DisplayName("First Part is null when only suffix length given.")
    void testGetFirstPartWhenEmpty() throws InvalidRangeException {
        var range = new Range("byte=-25");
        assertNull(range.getFirstPart());
    }

    @Test
    void testGetLastPart() throws InvalidRangeException {
        var range = new Range("byte=12-25");
        assertEquals("25", range.getLastPart());
    }

    @Test
    @DisplayName("Last Part is null when only first part is given.")
    void testGetLastPartWhenEmpty() throws InvalidRangeException {
        var range = new Range("byte=12-");
        assertNull(range.getLastPart());
    }

    @Test
    @DisplayName("Last Part is returned when first part is null.")
    void testGetLastPartWhenFirstIsEmpty() throws InvalidRangeException {
        var range = new Range("byte=-46");
        assertEquals("46", range.getLastPart());
    }

    @Test
    @DisplayName("First Part is returned when last part is null.")
    void testGetFirstPartWhenLastIsEmpty() throws InvalidRangeException {
        var range = new Range("byte=66-");
        assertEquals("66", range.getFirstPart());
    }

    @Test
    @DisplayName("Returns the correct unit.")
    void testGetUnit() throws InvalidRangeException {
        var range = new Range("meter=12-");
        assertEquals("meter", range.getUnit());
    }

    @Test
    @DisplayName("Throws InvalidRangeException .")
    void testInvalidRangeException() {
        try {
            new Range("meter=-");
            fail("Range did not throw exception.");
        } catch (InvalidRangeException e) {
            assertEquals("No values found for start and end.", e.getMessage());
        }
    }
}
