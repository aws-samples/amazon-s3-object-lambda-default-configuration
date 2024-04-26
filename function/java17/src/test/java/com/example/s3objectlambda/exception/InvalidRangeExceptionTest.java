package com.example.s3objectlambda.exception;

import com.example.s3objectlambda.error.Error;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class InvalidRangeExceptionTest {
    @Test
    @DisplayName("Test invalid range error")
    void testRangeGetError() {
        try {
            throw new InvalidRangeException("Invalid Range");
        } catch (InvalidRangeException e) {
            assertEquals(Error.INVALID_RANGE, e.getError());
        }
    }

    @Test
    @DisplayName("Test invalid range error message")
    void testRangeGetMessage() {
        try {
            throw new InvalidRangeException("Invalid Range Expected");
        } catch (InvalidRangeException e) {
            assertEquals("Invalid Range Expected", e.getMessage());
        }
    }

    @Test
    @DisplayName("Test invalid range exception to string.")
    void testRangeToString() {
        try {
            throw new InvalidRangeException("Invalid Range Expected");
        } catch (InvalidRangeException e) {
            assertEquals("com.example.s3objectlambda.exception.InvalidRangeException: " +
                    "Invalid Range Expected: added log message", e + ": added log message");
        }
    }
}
