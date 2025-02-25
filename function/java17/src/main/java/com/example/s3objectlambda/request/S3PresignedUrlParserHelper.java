package com.example.s3objectlambda.request;

import com.amazonaws.auth.internal.SignerConstants;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class S3PresignedUrlParserHelper {

    static final String X_AMZN_SIGNED_HEADERS_DELIMETER = ";";
    static final String QUERY_PARAM_DELIMETER = "&";
    static final String QUERY_PARAM_KEY_VALUE_DELIMETER = "=";

    public static List<String> retrieveSignedHeadersFromPresignedUrl(
        final String presignedUrl) throws MalformedURLException {

        URL url = new URL(presignedUrl);
        String query = url.getQuery();
        if (query == null) {
            return Collections.emptyList();
        }

        for (String queryParam : query.split(QUERY_PARAM_DELIMETER)) {

            String[] keyValuePair = queryParam.split(QUERY_PARAM_KEY_VALUE_DELIMETER, 2);
            if (keyValuePair.length != 2) {
                continue;
            }

            String key = keyValuePair[0];
            if (key.equals(SignerConstants.X_AMZ_SIGNED_HEADER)) {
                String decodedValue = URLDecoder.decode(keyValuePair[1], StandardCharsets.UTF_8);
                return Arrays.asList(decodedValue.split(X_AMZN_SIGNED_HEADERS_DELIMETER));
            }
        }

        return Collections.emptyList();
    }
}