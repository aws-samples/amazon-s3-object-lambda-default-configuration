package com.example.s3objectlambda.checksum;

/**
 * This interface represents the checksum generators for the response.
 * The implementing class method should return the Checksum object using the respective algorithm.
 */
public interface ChecksumGenerator {
    Checksum getChecksum(byte[] objectResponse) throws Exception;
}
