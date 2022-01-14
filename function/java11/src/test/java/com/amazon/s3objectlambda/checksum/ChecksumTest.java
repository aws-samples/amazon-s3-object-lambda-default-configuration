package com.amazon.s3objectlambda.checksum;


import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static com.amazon.s3objectlambda.checksum.Checksum.getChecksum;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class ChecksumTest {

    @Test
    public void getChecksumTest() {
        var originalData = "12345678910!".repeat(1000);
        var responseInputStream = originalData.getBytes(StandardCharsets.UTF_16);

        var md5Hash = new byte[0];
        try {
            md5Hash = MessageDigest.getInstance("MD5").digest(responseInputStream);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        var expectedDigestString = Base64.getEncoder().encodeToString(md5Hash);

        var checksumDigest = getChecksum(responseInputStream);
        var digest = checksumDigest.get("digest");

        assertEquals(expectedDigestString, digest);

    }
}
