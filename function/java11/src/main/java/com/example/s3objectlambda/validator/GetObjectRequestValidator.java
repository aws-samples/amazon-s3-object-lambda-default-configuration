package com.example.s3objectlambda.validator;

import com.example.s3objectlambda.request.GetObjectRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.Optional;

/**
 * Responsible for validating the user request. Returns an Optional string error message if there are errors
 * or Optional empty if valid.
 */
public class GetObjectRequestValidator implements RequestValidator {

    private Logger logger;
    private GetObjectRequestWrapper userRequest;

    public GetObjectRequestValidator(GetObjectRequestWrapper userRequest) {
        this.logger = LoggerFactory.getLogger(GetObjectRequestValidator.class);
        this.userRequest = userRequest;
    }

    /**
     * This method validates the user request.
     * @return Optional error message if the request is invalid. And returns optional empty if the request is valid.
     */
    public Optional<String> validateUserRequest()  {

        Optional range;
        Optional partNumber;

        try {
            range = this.userRequest.getRange();
            partNumber = this.userRequest.getPartNumber();
        } catch (URISyntaxException e) {
            this.logger.error("Exception in validation: " + e);
            return Optional.of("Invalid request URI");
        }

        if (range.isPresent() && partNumber.isPresent()) {
            return Optional.of("Cannot specify both Range header and partNumber query parameter");
        }

        return Optional.empty();
    }
}
