package com.example.s3objectlambda.validator;
import java.util.Optional;

/**
 * This interface will be implemented by the respective user request validator class.
 * The method,  validateUserRequest() should validate the user request.
 *
 */
public interface RequestValidator {
    Optional<String> validateUserRequest();
}
