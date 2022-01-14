package com.amazon.s3objectlambda.request;

import com.amazonaws.services.lambda.runtime.events.S3ObjectLambdaEvent.UserRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
/**
 * Contains utility methods for Request handling, such as extracting query parameters.
 */
public final class RequestUtil {

    static final String RANGE = "Range";
    static final String PART_NUMBER = "partNumber";

    private RequestUtil() {

    }

    public static String getPartNumber(UserRequest userRequest) {
        return getQueryParam(userRequest.getUrl(), PART_NUMBER);
    }

    public static String getRange(UserRequest userRequest) {
        var range = userRequest.getHeaders().get(RANGE);
        if (range == null) {
            range = getQueryParam(userRequest.getUrl(), RANGE);
        }
        return range;
    }

    private static String getQueryParam(String url, String partNumber) {
        try {
            var params = URLEncodedUtils.parse(new URI(url), "UTF-8");
            for (NameValuePair param : params) {
                if (param.getName().equals(partNumber)) {
                    return param.getValue();
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }
}
