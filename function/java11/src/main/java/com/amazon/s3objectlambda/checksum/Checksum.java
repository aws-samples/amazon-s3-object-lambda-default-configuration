package com.amazon.s3objectlambda.checksum;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates a checksum for the given object response.
 */
public final class Checksum {
    private static final String CHECKSUM_ALGORITHM = "MD5";

    private Checksum() {

    }

    public static Map<String, String> getChecksum(byte[] objectResponse) {
        var objectChecksum = new HashMap<String, String>();
        MessageDigest md;

        try {
            md = MessageDigest.getInstance(CHECKSUM_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        var digest = md.digest(objectResponse);

        objectChecksum.put("algorithm", CHECKSUM_ALGORITHM);
        objectChecksum.put("digest", Base64.getEncoder().encodeToString(digest));
        return objectChecksum;
    }
}
