package com.example.s3objectlambda.exception;

import com.example.s3objectlambda.error.Error;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class InvalidPartNumberExceptionTest {
    @Test
    @DisplayName("Test invalid part error.")
    void testPartNumberGetError() {
        try {
            throw new InvalidPartNumberException("Invalid Part");
        } catch (InvalidPartNumberException e) {
            assertEquals(Error.INVALID_PART, e.getError());
        }
    }

    @Test
    @DisplayName("Test invalid part error message.")
    void testPartNumberGetMessage() {
        try {
            throw new InvalidPartNumberException("Invalid Part Expected");
        } catch (InvalidPartNumberException e) {
            assertEquals("Invalid Part Expected", e.getMessage());
        }
    }

    @Test
    @DisplayName("Test invalid part exception to string.")
    void testPartNumberToString() {
        try {
            throw new InvalidPartNumberException("Invalid Part Expected");
        } catch (InvalidPartNumberException e) {
            assertEquals("com.example.s3objectlambda.exception.InvalidPartNumberException: " +
                    "Invalid Part Expected: added log message", e + ": added log message");
        }
    }
}
