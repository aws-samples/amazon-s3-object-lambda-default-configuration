package com.amazon.s3objectlambda.defaultconfig;

import org.apache.commons.lang3.RandomStringUtils;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.Parameter;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sts.StsClient;

public class SdkHelper {

    public DefaultCredentialsProvider getCredential() {
        return DefaultCredentialsProvider.builder().build();
    }

    public S3Client getS3Client(String region) {
        return S3Client.builder()
                .credentialsProvider(getCredential())
                .region(Region.of(region))
                .build();
    }

    public CloudFormationClient getCloudFormationClient(String region) {
        return CloudFormationClient.builder()
                .credentialsProvider(getCredential())
                .region(Region.of(region))
                .build();
    }

    public Parameter buildParameter(String key, String value) {
        return Parameter.builder()
                .parameterKey(key)
                .parameterValue(value)
                .build();
    }

    public String getOLAccessPointArn(String region, String accessPointName) {
        String accountID = getAWSAccountID(region);
        return String.format("arn:aws:s3-object-lambda:%s:%s:accesspoint/%s", region, accountID, accessPointName);
    }

    public String generateRandomResourceName(int count) {
        return RandomStringUtils.randomAlphabetic(count).toLowerCase();
    }

    public String getAWSAccountID(String region) {
        var stsClient = StsClient.builder()
                                           .credentialsProvider(getCredential())
                                           .region(Region.of(region))
                                           .build();
        return stsClient.getCallerIdentity().account();
    }
}
