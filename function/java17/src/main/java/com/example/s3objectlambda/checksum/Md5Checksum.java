package com.example.s3objectlambda.checksum;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Generates an MD5 checksum for the given object response.
 */
public  class Md5Checksum implements ChecksumGenerator {

    private static final String ALGORITHM = "MD5";
    public Checksum getChecksum(byte[] objectResponse) throws NoSuchAlgorithmException {

        MessageDigest md;
        md = MessageDigest.getInstance(ALGORITHM);
        var digest = md.digest(objectResponse);

        var checksum = Base64.getEncoder().encodeToString(digest);
        return new Checksum(ALGORITHM, checksum);
    }
}
