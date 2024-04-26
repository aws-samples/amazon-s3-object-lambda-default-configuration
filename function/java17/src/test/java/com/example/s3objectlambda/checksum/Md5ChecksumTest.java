package com.example.s3objectlambda.checksum;


import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class Md5ChecksumTest {

    @Test
    public void getChecksumTest() throws NoSuchAlgorithmException {
        var originalData = "12345678910!".repeat(1000);
        var responseInputStream = originalData.getBytes(StandardCharsets.UTF_16);


        var md5Hash = MessageDigest.getInstance("MD5").digest(responseInputStream);

        var expectedDigestString = Base64.getEncoder().encodeToString(md5Hash);

        var checksum = new Md5Checksum().getChecksum(responseInputStream);
        var digest = checksum.getChecksum();

        assertEquals(expectedDigestString, digest);

    }
}
