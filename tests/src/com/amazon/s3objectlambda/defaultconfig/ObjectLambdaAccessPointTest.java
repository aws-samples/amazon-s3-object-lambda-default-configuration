package com.amazon.s3objectlambda.defaultconfig;

import static com.amazon.s3objectlambda.defaultconfig.KeyConstants.*;

import org.testng.ITestContext;
import org.testng.annotations.*;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.crypto.KeyGenerator;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class ObjectLambdaAccessPointTest {

    protected static final int DEFAULT_PART_SIZE = 5242880; // 5 MB
    protected static final String DUMMY_ACCOUNT_ID = "111122223333";
    protected final String data = "1234567890".repeat(10);
    protected final String largeData = "123".repeat(DEFAULT_PART_SIZE);
    protected S3Client s3Client;
    protected String olAccessPointName;
    protected String olapArn;
    protected String s3BucketName;
    protected SdkHelper sdkHelper;
    protected Region region;

    @Parameters({"region", "s3BucketName"})
    @BeforeClass(alwaysRun = true)
    void setup(ITestContext context, String region, String s3BucketName) {
        this.olAccessPointName = (String) context.getAttribute(OL_AP_NAME_KEY);
        this.sdkHelper = new SdkHelper();
        this.s3Client = sdkHelper.getS3Client(region);
        this.region = Region.of(region);
        this.olapArn = sdkHelper.getOLAccessPointArn(this.region, olAccessPointName);
        this.s3BucketName = s3BucketName;
    }

    protected PutObjectResponse setupResource(String objectKey, String data) {
        return s3Client.putObject(builder -> builder.bucket(s3BucketName).key(objectKey).build(),
                RequestBody.fromString(data, StandardCharsets.UTF_8));
    }

    protected void setupResources(String data, String... objectKeys) {
        for (String objectKey: objectKeys) {
            s3Client.putObject(builder -> builder.bucket(s3BucketName).key(objectKey).build(),
                               RequestBody.fromString(data, StandardCharsets.UTF_8));
        }
    }

    protected void cleanupResources(String... objectKeys) {
        for(String objectKey : objectKeys) {
            s3Client.deleteObject(builder -> builder.bucket(s3BucketName).key(objectKey).build());
        }
    }

    protected PutObjectResponse setupResourceWithChecksum(String objectKey, String data) {
        return s3Client.putObject(builder -> builder.bucket(s3BucketName)
                                          .checksumAlgorithm(ChecksumAlgorithm.CRC32)
                                          .key(objectKey)
                                          .build(),
                                  RequestBody.fromString(data, StandardCharsets.UTF_8));
    }

    protected void cleanupResource(String objectKey) {
        s3Client.deleteObject(builder -> builder.bucket(s3BucketName).key(objectKey).build());
    }

    protected byte[] generateSecretKey() {
        KeyGenerator generator;
        try {
            generator = KeyGenerator.getInstance("AES");
            generator.init(256, new SecureRandom());
            return generator.generateKey().getEncoded();
        } catch (Exception e) {
            return null;
        }
    }

}
