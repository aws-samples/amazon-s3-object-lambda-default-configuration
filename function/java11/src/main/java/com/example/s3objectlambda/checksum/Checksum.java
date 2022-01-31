package com.example.s3objectlambda.checksum;

/**
 * This class represents the checksum for the response object using any algorithm that the generator implements.
 */
public class Checksum {

    private String algorithm;
    private String checksum;

    public Checksum(String algorithm, String checksum) {
        this.algorithm = algorithm;
        this.checksum = checksum;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
