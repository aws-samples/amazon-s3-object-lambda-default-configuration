package com.example.s3objectlambda.request;

import com.example.s3objectlambda.exception.InvalidUrlException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class S3PresignedUrlParserHelper {

    static final String X_AMZN_SIGNED_HEADERS = "X-Amz-SignedHeaders";
    static final String X_AMZN_SIGNED_HEADERS_DELIMETER = ";";
    static final String QUERY_PARAM_DELIMETER = "&";
    static final String QUERY_PARAM_KEY_VALUE_DELIMETER = "=";

    public static List<String> retrieveSignedHeadersFromPresignedUrl(
        final String presignedUrl) throws InvalidUrlException {

        URL url;
        try {
            url = new URL(presignedUrl);
        } catch (MalformedURLException e) {
            throw new InvalidUrlException("Could not parse URL exception.");
        }

        String query = url.getQuery();
        if (query == null) {
            return Collections.emptyList();
        }

        for (String queryParam : query.split(QUERY_PARAM_DELIMETER)) {
            String[] keyValue = queryParam.split(QUERY_PARAM_KEY_VALUE_DELIMETER, 2);
            if (keyValue.length == 2 && keyValue[0].equals(X_AMZN_SIGNED_HEADERS)) {
                String decodedValue = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);

                for (String value : decodedValue.split(X_AMZN_SIGNED_HEADERS_DELIMETER)) {
                    System.out.println("EXTRACTED HEADER: " + value);
                }

                return Arrays.asList(decodedValue.split(X_AMZN_SIGNED_HEADERS_DELIMETER));
            }
        }

        return Collections.emptyList();
    }
}